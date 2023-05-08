package com.example.translate.info

import com.github.shadowsocks.database.Profile

data class ConnectInfo(
    val profileList: MutableList<Profile>,
    val countryPerList: MutableList<CountryPerInfo>
)