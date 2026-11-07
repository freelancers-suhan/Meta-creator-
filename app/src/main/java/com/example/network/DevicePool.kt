package com.example.network

import java.security.SecureRandom

data class DeviceProfile(
    val model: String,
    val androidVersion: String,
    val buildId: String,
    val chromeVersion: String
)

object DevicePool {

    private val random = SecureRandom()

    val ALL_DEVICES: List<DeviceProfile> = listOf(
        // === Samsung Galaxy Series (32 Devices) ===
        DeviceProfile("SM-S928B", "14.0.0", "UP1A.231005.007", "128.0.6613.88"), // S24 Ultra
        DeviceProfile("SM-S926B", "14.0.0", "UP1A.231005.007", "128.0.6613.88"), // S24+
        DeviceProfile("SM-S921B", "14.0.0", "UP1A.231005.007", "128.0.6613.127"), // S24
        DeviceProfile("SM-S918B", "14.0.0", "UP1A.231005.007", "126.0.6478.134"), // S23 Ultra
        DeviceProfile("SM-S916B", "14.0.0", "UP1A.231005.007", "126.0.6478.134"), // S23+
        DeviceProfile("SM-S911B", "14.0.0", "UP1A.231005.007", "127.0.6533.64"),  // S23
        DeviceProfile("SM-S908B", "14.0.0", "UP1A.231005.007", "125.0.6422.165"), // S22 Ultra
        DeviceProfile("SM-S901B", "13.0.0", "TP1A.220624.014", "124.0.6367.179"), // S22
        DeviceProfile("SM-G998B", "13.0.0", "TP1A.220624.014", "123.0.6312.118"), // S21 Ultra
        DeviceProfile("SM-G991B", "13.0.0", "TP1A.220624.014", "123.0.6312.118"), // S21
        DeviceProfile("SM-G990B", "14.0.0", "UP1A.231005.007", "127.0.6533.64"),  // S21 FE
        DeviceProfile("SM-F946B", "14.0.0", "UP1A.231005.007", "128.0.6613.88"),  // Z Fold 5
        DeviceProfile("SM-F731B", "14.0.0", "UP1A.231005.007", "127.0.6533.64"),  // Z Flip 5
        DeviceProfile("SM-A556B", "14.0.0", "UP1A.231005.007", "128.0.6613.88"),  // A55 5G
        DeviceProfile("SM-A546B", "14.0.0", "UP1A.231005.007", "128.0.6613.88"),  // A54 5G
        DeviceProfile("SM-A536B", "13.0.0", "TP1A.220624.014", "125.0.6422.165"), // A53 5G
        DeviceProfile("SM-A528B", "13.0.0", "TP1A.220624.014", "124.0.6367.113"), // A52s 5G
        DeviceProfile("SM-A525F", "12.0.0", "SP1A.210812.016", "122.0.6261.119"), // A52 4G
        DeviceProfile("SM-A356B", "14.0.0", "UP1A.231005.007", "128.0.6613.88"),  // A35 5G
        DeviceProfile("SM-A346B", "14.0.0", "UP1A.231005.007", "127.0.6533.64"),  // A34 5G
        DeviceProfile("SM-A336B", "13.0.0", "TP1A.220624.014", "125.0.6422.165"), // A33 5G
        DeviceProfile("SM-A256B", "14.0.0", "UP1A.231005.007", "127.0.6533.64"),  // A25 5G
        DeviceProfile("SM-A245F", "14.0.0", "UP1A.231005.007", "126.0.6478.122"), // A24
        DeviceProfile("SM-A156B", "14.0.0", "UP1A.231005.007", "128.0.6613.88"),  // A15 5G
        DeviceProfile("SM-A145F", "13.0.0", "TP1A.220624.014", "125.0.6422.165"), // A14
        DeviceProfile("SM-A135F", "12.0.0", "SP1A.210812.016", "123.0.6312.118"), // A13
        DeviceProfile("SM-A127F", "11.0.0", "RP1A.200720.012", "121.0.6167.178"), // A12 Nacho
        DeviceProfile("SM-A057F", "14.0.0", "UP1A.231005.007", "127.0.6533.64"),  // A05s
        DeviceProfile("SM-A047F", "13.0.0", "TP1A.220624.014", "124.0.6367.113"), // A04s
        DeviceProfile("SM-M346B", "14.0.0", "UP1A.231005.007", "127.0.6533.64"),  // M34 5G
        DeviceProfile("SM-M146B", "13.0.0", "TP1A.220624.014", "125.0.6422.165"), // M14 5G
        DeviceProfile("SM-N986B", "13.0.0", "TP1A.220624.014", "123.0.6312.118"), // Note 20 Ultra

        // === Xiaomi / Redmi / POCO Series (28 Devices) ===
        DeviceProfile("23127PN0CG", "14.0.0", "UKQ1.231003.002", "128.0.6613.88"),  // Xiaomi 14
        DeviceProfile("2210132G", "13.0.0", "TKQ1.221114.001", "126.0.6478.134"),   // Xiaomi 13 Pro
        DeviceProfile("2306EPN60G", "14.0.0", "UKQ1.231003.002", "127.0.6533.64"),  // Xiaomi 13T
        DeviceProfile("2201123G", "13.0.0", "TKQ1.221114.001", "125.0.6422.165"),   // Xiaomi 12
        DeviceProfile("23090RA98G", "14.0.0", "UKQ1.231003.002", "128.0.6613.88"),  // Redmi Note 13 Pro+
        DeviceProfile("23117RA68G", "14.0.0", "UKQ1.231003.002", "127.0.6533.64"),  // Redmi Note 13 Pro
        DeviceProfile("2312DRAABG", "14.0.0", "UKQ1.231003.002", "127.0.6533.64"),  // Redmi Note 13 4G
        DeviceProfile("22101316G", "13.0.0", "TKQ1.221114.001", "126.0.6478.122"),  // Redmi Note 12 Pro
        DeviceProfile("23021RAAEG", "13.0.0", "TKQ1.221114.001", "125.0.6422.165"), // Redmi Note 12
        DeviceProfile("2201117TG", "12.0.0", "SKQ1.211103.001", "123.0.6312.118"),  // Redmi Note 11
        DeviceProfile("2201116TG", "12.0.0", "SKQ1.211103.001", "123.0.6312.118"),  // Redmi Note 11 Pro
        DeviceProfile("M2101K6G", "12.0.0", "RKQ1.200826.002", "121.0.6167.178"),   // Redmi Note 10 Pro
        DeviceProfile("M2101K7AG", "12.0.0", "RKQ1.200826.002", "121.0.6167.178"),  // Redmi Note 10
        DeviceProfile("23100RN82L", "13.0.0", "TP1A.220624.014", "126.0.6478.122"), // Redmi 13C
        DeviceProfile("23053RN02Y", "13.0.0", "TP1A.220624.014", "125.0.6422.165"), // Redmi 12
        DeviceProfile("220333QAG", "12.0.0", "SKQ1.211103.001", "123.0.6312.118"),  // Redmi 10C
        DeviceProfile("M2006C3LG", "10.0.0", "QP1A.190711.020", "118.0.5993.111"),  // Redmi 9A
        DeviceProfile("2311DRK48G", "14.0.0", "UKQ1.231003.002", "128.0.6613.88"),  // POCO X6 Pro
        DeviceProfile("23122PCD1G", "14.0.0", "UKQ1.231003.002", "127.0.6533.64"),  // POCO X6
        DeviceProfile("23049PCD8G", "14.0.0", "UKQ1.231003.002", "127.0.6533.64"),  // POCO F5
        DeviceProfile("22101320G", "13.0.0", "TKQ1.221114.001", "126.0.6478.122"),  // POCO X5 Pro
        DeviceProfile("2312FPCA6G", "14.0.0", "UKQ1.231003.002", "127.0.6533.64"),  // POCO M6 Pro
        DeviceProfile("22071219CG", "13.0.0", "TKQ1.221114.001", "125.0.6422.165"), // POCO M5
        DeviceProfile("2310FPCA4G", "13.0.0", "TP1A.220624.014", "125.0.6422.165"), // POCO C65
        DeviceProfile("M2012K11AG", "13.0.0", "TKQ1.221114.001", "124.0.6367.113"), // POCO F3
        DeviceProfile("M2007J20CG", "12.0.0", "SKQ1.211019.001", "122.0.6261.119"), // POCO X3 NFC
        DeviceProfile("2109119DG", "13.0.0", "TKQ1.221114.001", "124.0.6367.113"),  // Xiaomi 11 Lite 5G NE
        DeviceProfile("21061119AG", "12.0.0", "SKQ1.211019.001", "122.0.6261.119"), // POCO X3 GT

        // === Realme & Oppo Series (20 Devices) ===
        DeviceProfile("RMX3840", "14.0.0", "UKQ1.230924.001", "128.0.6613.88"), // Realme 12 Pro+
        DeviceProfile("RMX3867", "14.0.0", "UKQ1.230924.001", "127.0.6533.64"), // Realme 12
        DeviceProfile("RMX3771", "14.0.0", "UKQ1.230924.001", "127.0.6533.64"), // Realme 11 Pro
        DeviceProfile("RMX3780", "13.0.0", "TP1A.220905.001", "125.0.6422.165"), // Realme 11
        DeviceProfile("RMX3630", "13.0.0", "TP1A.220905.001", "124.0.6367.113"), // Realme 10
        DeviceProfile("RMX3472", "13.0.0", "TP1A.220905.001", "123.0.6312.118"), // Realme 9 Pro
        DeviceProfile("RMX3710", "13.0.0", "TP1A.220905.001", "125.0.6422.165"), // Realme C55
        DeviceProfile("RMX3760", "13.0.0", "TP1A.220905.001", "125.0.6422.165"), // Realme C53
        DeviceProfile("RMX3511", "12.0.0", "SP1A.210812.016", "122.0.6261.119"), // Realme C35
        DeviceProfile("RMX3263", "11.0.0", "RP1A.201005.001", "120.0.6099.144"), // Realme C21Y
        DeviceProfile("RMX3301", "14.0.0", "UKQ1.230924.001", "127.0.6533.64"),  // Realme GT 2 Pro
        DeviceProfile("CPH2607", "14.0.0", "UKQ1.230924.001", "128.0.6613.88"),  // Oppo Reno 11 Pro
        DeviceProfile("CPH2531", "14.0.0", "UKQ1.230924.001", "127.0.6533.64"),  // Oppo Reno 10
        DeviceProfile("CPH2359", "13.0.0", "TP1A.220905.001", "125.0.6422.165"), // Oppo Reno 8
        DeviceProfile("CPH2343", "13.0.0", "TP1A.220905.001", "127.0.6533.64"),  // Oppo Reno 7
        DeviceProfile("CPH2565", "13.0.0", "TP1A.220905.001", "126.0.6478.122"), // Oppo A78
        DeviceProfile("CPH2577", "13.0.0", "TP1A.220905.001", "125.0.6422.165"), // Oppo A58
        DeviceProfile("CPH2579", "13.0.0", "TP1A.220905.001", "125.0.6422.165"), // Oppo A38
        DeviceProfile("CPH2591", "13.0.0", "TP1A.220905.001", "124.0.6367.113"), // Oppo A18
        DeviceProfile("CPH2305", "14.0.0", "UKQ1.230924.001", "127.0.6533.64"),  // Oppo Find X5 Pro

        // === Vivo & iQOO Series (16 Devices) ===
        DeviceProfile("V2318", "14.0.0", "UP1A.231005.007", "128.0.6613.88"),  // Vivo V30
        DeviceProfile("V2250", "13.0.0", "TP1A.220624.014", "126.0.6478.122"), // Vivo V29
        DeviceProfile("V2246", "13.0.0", "TP1A.220624.014", "125.0.6422.165"), // Vivo V27
        DeviceProfile("V2249", "13.0.0", "TP1A.220624.014", "125.0.6422.165"), // Vivo Y27
        DeviceProfile("V2310", "13.0.0", "TP1A.220624.014", "124.0.6367.113"), // Vivo Y17s
        DeviceProfile("V2247", "13.0.0", "TP1A.220624.014", "125.0.6422.165"), // Vivo Y36
        DeviceProfile("V2207", "12.0.0", "SP1A.210812.003", "122.0.6261.119"), // Vivo Y22
        DeviceProfile("V2225", "13.0.0", "TP1A.220624.014", "124.0.6367.113"), // Vivo T2x
        DeviceProfile("V2309", "14.0.0", "UP1A.231005.007", "128.0.6613.88"),  // Vivo X100 Pro
        DeviceProfile("V2218", "13.0.0", "TP1A.220624.014", "125.0.6422.165"), // Vivo X90
        DeviceProfile("V2130", "12.0.0", "SP1A.210812.003", "121.0.6167.178"), // Vivo V23
        DeviceProfile("I2217", "14.0.0", "UP1A.231005.007", "128.0.6613.88"),  // iQOO Neo 9 Pro
        DeviceProfile("I2301", "13.0.0", "TP1A.220624.014", "126.0.6478.122"), // iQOO Z7 Pro
        DeviceProfile("I2220", "14.0.0", "UP1A.231005.007", "128.0.6613.88"),  // iQOO 12
        DeviceProfile("I2127", "13.0.0", "TP1A.220624.014", "124.0.6367.113"), // iQOO Z6
        DeviceProfile("V2058", "12.0.0", "SP1A.210812.003", "120.0.6099.144"), // Vivo Y21

        // === Infinix, Tecno & itel Series (16 Devices) ===
        DeviceProfile("X6850", "14.0.0", "UP1A.231005.007", "128.0.6613.88"),  // Infinix Note 40 Pro
        DeviceProfile("X6833B", "13.0.0", "TP1A.220624.014", "126.0.6478.122"), // Infinix Note 30
        DeviceProfile("X6837", "13.0.0", "TP1A.220624.014", "126.0.6478.122"),  // Infinix Hot 40 Pro
        DeviceProfile("X669", "13.0.0", "TP1A.220624.014", "125.0.6422.165"),   // Infinix Hot 30
        DeviceProfile("X6525", "13.0.0", "TP1A.220624.014", "124.0.6367.113"),  // Infinix Smart 8
        DeviceProfile("X6739", "13.0.0", "TP1A.220624.014", "126.0.6478.122"),  // Infinix GT 10 Pro
        DeviceProfile("X6731", "13.0.0", "TP1A.220624.014", "125.0.6422.165"),  // Infinix Zero 30
        DeviceProfile("CL6", "14.0.0", "UP1A.231005.007", "128.0.6613.88"),    // Tecno Camon 30
        DeviceProfile("CK6", "13.0.0", "TP1A.220624.014", "125.0.6422.165"),    // Tecno Camon 20
        DeviceProfile("KJ6", "13.0.0", "TP1A.220624.014", "125.0.6422.165"),    // Tecno Spark 20 Pro
        DeviceProfile("KI5q", "13.0.0", "TP1A.220624.014", "124.0.6367.113"),   // Tecno Spark 10
        DeviceProfile("LH7n", "13.0.0", "TP1A.220624.014", "125.0.6422.165"),   // Tecno Pova 5
        DeviceProfile("S666LN", "13.0.0", "TP1A.220624.014", "126.0.6478.122"), // itel S23+
        DeviceProfile("S665L", "12.0.0", "SP1A.210812.016", "124.0.6367.113"),  // itel S23
        DeviceProfile("A665L", "13.0.0", "TP1A.220624.014", "124.0.6367.113"),  // itel A70
        DeviceProfile("P663LN", "13.0.0", "TP1A.220624.014", "125.0.6422.165"), // itel P55+

        // === OnePlus & Google Pixel (12 Devices) ===
        DeviceProfile("CPH2581", "14.0.0", "UKQ1.230924.001", "128.0.6613.88"), // OnePlus 12
        DeviceProfile("CPH2449", "14.0.0", "UKQ1.230924.001", "127.0.6533.64"), // OnePlus 11
        DeviceProfile("NE2213", "13.0.0", "TP1A.220905.001", "125.0.6422.165"), // OnePlus 10 Pro
        DeviceProfile("CPH2569", "13.0.0", "TP1A.220905.001", "126.0.6478.122"), // OnePlus Nord CE 3
        DeviceProfile("CPH2409", "13.0.0", "TP1A.220905.001", "124.0.6367.113"), // OnePlus Nord CE 2 Lite
        DeviceProfile("CPH2493", "14.0.0", "UKQ1.230924.001", "127.0.6533.64"), // OnePlus Nord 3
        DeviceProfile("Pixel 8 Pro", "14.0.0", "UQ1A.240205.004", "128.0.6613.88"),
        DeviceProfile("Pixel 8", "14.0.0", "UQ1A.240205.004", "128.0.6613.88"),
        DeviceProfile("Pixel 7a", "14.0.0", "UQ1A.240205.004", "127.0.6533.64"),
        DeviceProfile("Pixel 7 Pro", "14.0.0", "UQ1A.240205.004", "128.0.6613.88"),
        DeviceProfile("Pixel 7", "14.0.0", "UQ1A.240205.004", "128.0.6613.88"),
        DeviceProfile("Pixel 6a", "13.0.0", "TQ3A.230901.001", "124.0.6367.113"),

        // === Motorola, Nothing, Honor, Sony, Asus (12 Devices) ===
        DeviceProfile("moto g54 5G", "13.0.0", "TP1A.220624.014", "126.0.6478.122"),
        DeviceProfile("moto g84 5G", "13.0.0", "TP1A.220624.014", "126.0.6478.122"),
        DeviceProfile("motorola edge 40", "13.0.0", "TP1A.220624.014", "125.0.6422.165"),
        DeviceProfile("A065", "14.0.0", "UP1A.231005.007", "128.0.6613.88"),  // Nothing Phone (2)
        DeviceProfile("A063", "13.0.0", "TP1A.220624.014", "124.0.6367.113"), // Nothing Phone (1)
        DeviceProfile("A142", "14.0.0", "UP1A.231005.007", "128.0.6613.88"),  // Nothing Phone (2a)
        DeviceProfile("REA-NX9", "13.0.0", "TP1A.220624.014", "126.0.6478.122"), // Honor 90
        DeviceProfile("ALI-NX1", "13.0.0", "TP1A.220624.014", "125.0.6422.165"), // Honor X9b
        DeviceProfile("XQ-DQ72", "14.0.0", "67.1.A.2.220", "127.0.6533.64"),   // Sony Xperia 1 V
        DeviceProfile("XQ-DE72", "13.0.0", "64.1.A.0.992", "124.0.6367.113"),   // Sony Xperia 5 IV
        DeviceProfile("ASUS_AI2202", "13.0.0", "TKQ1.220807.001", "125.0.6422.165"), // Asus Zenfone 9
        DeviceProfile("ASUS_AI2205", "14.0.0", "UKQ1.230924.001", "128.0.6613.88")  // Asus ROG Phone 7
    )

    fun getRandomDevice(): DeviceProfile {
        return ALL_DEVICES[random.nextInt(ALL_DEVICES.size)]
    }

    fun getTotalCount(): Int = ALL_DEVICES.size
}
