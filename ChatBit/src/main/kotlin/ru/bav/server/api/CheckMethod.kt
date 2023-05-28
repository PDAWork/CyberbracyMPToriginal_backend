package ru.bav.server.api

object CheckMethod {
    fun from(num:Int) : String {
        /*
        Метод проверки  соответствия ОТ (1-рассмотрение документов; 2-  осмотр и обследование
объектов (производственных объектов, территорий, транспортных средств и др.); 3- отбор образцов продукции, проб и т.п.; 4-
проведение исследований, испытаний образцов продукции, проб; 5- измерение параметров функционирования различных сетей (электроэнергетики, газоснабжения, водоснабжения и др.); 6 - наблюдение за соблюдением ОТ; 7 -контрольная закупка; 8 -
иное)
         */
        return when(num){
            1->"Рассмотрение документов"
            2->"Осмотр и обследование объектов (производственных объектов, территорий, транспортных средств и др.)"
            3->"Отбор образцов продукции, проб и т.п."
            4->"Проведение исследований, испытаний образцов продукции, проб"
            5->"Измерение параметров функционирования различных сетей (электроэнергетики, газоснабжения, водоснабжения и др.)"
            6->"Наблюдение за соблюдением ОТ"
            7->"Контрольная закупка"
            8->"Иное"
            else->"Unknown"
        }
    }
}