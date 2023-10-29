package ru.ssnexus.taganwater

data class ScrappingResult(val notifications: MutableList<String> = mutableListOf(), var count:Int = 0)
