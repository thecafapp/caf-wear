/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.micahlindley.tca_wear.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.zIndex
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TitleCard
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.micahlindley.tca_wear.CafMenu
import com.micahlindley.tca_wear.MealTime
import com.micahlindley.tca_wear.presentation.theme.TcawearTheme
import kotlinx.coroutines.delay
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.rememberColumnState
import com.google.android.horologist.compose.rotaryinput.DefaultRotaryHapticHandler
import com.google.android.horologist.compose.rotaryinput.RotaryDefaults
import com.google.android.horologist.compose.rotaryinput.RotaryHapticHandler
import com.google.android.horologist.compose.rotaryinput.RotaryHapticsType
import com.google.android.horologist.compose.rotaryinput.rememberRotaryHapticHandler
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.micahlindley.tca_wear.R
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp()
        }
    }
}

fun fetchMenu(context: Context, result: MutableState<CafMenu>) {
    val queue = Volley.newRequestQueue(context);
    val url = "https://thecaf.app/api/menu";

    val menuRequest = JsonObjectRequest(Request.Method.GET, url, null,
        { response ->
            result.value.date.value = response.getString("date");
            val meals = response.getJSONArray("meals");
            result.value.menu.clear();
            result.value.mealCount.value = 0;
            for (i in 0 until meals.length()) {
                val obj = meals.getJSONObject(i);
                val m = MealTime();
                m.name.value = obj.getString("name");
                m.start.value = Date(obj.getLong("start"));
                m.end.value = Date(obj.getLong("end"));
                val foods = obj.getJSONArray("menu");
                var s = "";
                for (j in 0 until foods.length()) {
                    s += foods.getString(j) + "\n";
                }
                if (foods.length() < 1) {
                    s = "No menu available"
                }
                s = s.trimEnd();
                m.items.value = s;
                if (m.end.value.after(Date())) {
                    result.value.menu.add(m);
                    result.value.mealCount.intValue++;
                }
            }
        },
        { error ->
            result.value.date.value = "ERROR";
        })

    queue.add(menuRequest);
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun WearApp() {
    val context = LocalContext.current;
    val response = remember { mutableStateOf(CafMenu()) };
    val prettyTime = PrettyTime(Locale.getDefault())
    val listState = rememberColumnState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    fetchMenu(context, response);

    TcawearTheme {

        Scaffold(
            positionIndicator = {
                PositionIndicator(listState.state);
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background),
                contentAlignment = Alignment.Center,
            ) {
                Vignette(
                    vignettePosition = VignettePosition.TopAndBottom,
                    modifier = Modifier.zIndex(3F)
                )
                ScalingLazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2F)
                        .focusRequester(focusRequester)
                        .focusable()
                        .rotaryWithScroll(focusRequester, listState.state, rotaryHaptics = rememberRotaryHapticHandler(listState.state)),
                    columnState = listState,
                ) {
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(R.drawable.logo),
                                contentDescription = "The Caf App",
                                tint = colorResource(R.color.accent),
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                            if (response.value.date.value != "") {
                                val meal = response.value.menu[0];
                                var mealText = "";
                                if (meal.start.value.after(Date())) {
                                    mealText = "Next meal is"
                                } else {
                                    mealText = "Meal ends"
                                }
                                Text(
                                    mealText + "\n" +
                                            prettyTime.format(response.value.menu[0].start.value),
                                    modifier = Modifier.padding(bottom = 5.dp),
                                    fontSize = 2.4.em,
                                    lineHeight = 1.em,
                                    textAlign = TextAlign.Center
                                );
                            }
                        }
                    }
                    items(response.value.mealCount.intValue) { index ->
                        TitleCard(
                            title = {
                                Text(response.value.menu[index].name.value)
                            },
                            time = {
                                val f = SimpleDateFormat("h:mma");
                                Text(f.format(response.value.menu[index].start.value));
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 0.dp),
                            onClick = {},
                            content = {
                                Text(response.value.menu[index].items.value)
                            },
                            enabled = false
                        );
                    }
                    item {
                        Button(
                            onClick = {
                                fetchMenu(context, response);
                                coroutineScope.launch {
                                    listState.state.animateScrollToItem(0);
                                }
                            },
                            modifier = Modifier.padding(top = 10.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_refresh_24),
                                contentDescription = "Refresh"
                            )
                        }
                    }
                }
                LaunchedEffect(Unit) {
                    delay(50)
                    focusRequester.requestFocus()
                }
            }
        }
    }
}
