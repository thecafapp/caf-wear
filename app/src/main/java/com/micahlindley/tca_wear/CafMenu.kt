package com.micahlindley.tca_wear

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import java.time.Instant
import java.util.Date

public class CafMenu {
    public var date: MutableState<String> = mutableStateOf("");
    public var menu: MutableList<MealTime> = mutableListOf(MealTime());
    public var mealCount: MutableIntState = mutableIntStateOf(0);
}

public class MealTime {
    public var name: MutableState<String> = mutableStateOf("");
    public var start: MutableState<Date> = mutableStateOf(Date.from(Instant.now()));
    public var end: MutableState<Date> = mutableStateOf(Date.from(Instant.now()));
    public var items: MutableState<String> = mutableStateOf("");
}