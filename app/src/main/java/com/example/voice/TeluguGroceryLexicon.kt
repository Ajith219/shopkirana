package com.example.voice

object TeluguGroceryLexicon {

    // Number word mapping to Double values
    val NUMBER_WORDS: Map<String, Double> = mapOf(
        // English
        "one" to 1.0, "a" to 1.0, "an" to 1.0, "two" to 2.0, "three" to 3.0,
        "four" to 4.0, "five" to 5.0, "six" to 6.0, "seven" to 7.0, "eight" to 8.0,
        "nine" to 9.0, "ten" to 10.0, "twelve" to 12.0, "dozen" to 12.0,
        "half" to 0.5, "quarter" to 0.25,
        
        // Telugu Script
        "ఒకటి" to 1.0, "ఒక" to 1.0, "రెండు" to 2.0, "మూడు" to 3.0, "నాలుగు" to 4.0,
        "ఐదు" to 5.0, "ఆరు" to 6.0, "ఏడు" to 7.0, "ఎనిమిది" to 8.0, "తొమ్మిది" to 9.0,
        "పది" to 10.0, "డజను" to 12.0, "డజన్" to 12.0, "సగం" to 0.5, "పావు" to 0.25,
        "ముప్పావు" to 0.75, "రెండున్నర" to 2.5, "ఒకటిన్నర" to 1.5,
        
        // Tanglish / Romanized Telugu
        "okati" to 1.0, "oka" to 1.0, "rendu" to 2.0, "moodu" to 3.0, "naalugu" to 4.0,
        "aidu" to 5.0, "aaru" to 6.0, "yedu" to 7.0, "edu" to 7.0, "enimidi" to 8.0,
        "tommidi" to 9.0, "padi" to 10.0, "ara" to 0.5, "paavu" to 0.25, "muppaavu" to 0.75,
        "dajan" to 12.0, "rendunnara" to 2.5, "okatinnara" to 1.5
    )

    // Unit word mapping to normalized unit string
    val UNIT_WORDS: Map<String, String> = mapOf(
        // Kilograms
        "kg" to "kg", "kgs" to "kg", "kilo" to "kg", "kilos" to "kg", "kilogram" to "kg",
        "కేజీ" to "kg", "కేజీలు" to "kg", "కిలో" to "kg", "కిలోలు" to "kg", "kilolu" to "kg",
        
        // Grams
        "g" to "g", "gm" to "g", "gms" to "g", "gram" to "g", "grams" to "g",
        "గ్రాము" to "g", "గ్రాములు" to "g", "గ్రామ్స్" to "g", "gramulu" to "g",
        
        // Liters
        "l" to "l", "ltr" to "l", "liter" to "l", "liters" to "l", "litre" to "l", "litres" to "l",
        "లీటర్" to "l", "లీటర్లు" to "l", "litarlu" to "l",
        
        // Packets
        "pkt" to "pkt", "packet" to "pkt", "packets" to "pkt", "pack" to "pkt", "packs" to "pkt",
        "ప్యాకెట్" to "pkt", "ప్యాకెట్లు" to "pkt", "packetlu" to "pkt",
        
        // Pieces
        "piece" to "piece", "pieces" to "piece", "pcs" to "piece", "pc" to "piece",
        "ముక్క" to "piece", "ముక్కలు" to "piece", "mukkalu" to "piece"
    )

    // Action phrase keywords
    val SALES_PHRASES = listOf(
        "today's sales", "today sales", "show sales", "sales summary", "daily sales",
        "eroju sales", "eroju ammakalu", "sales chudu", "ammakam entha", "sales entha",
        "అమ్మకాలు", "ఈరోజు అమ్మకాలు", "సేల్స్", "సేల్స్ చూడు"
    )

    val STOCK_PHRASES = listOf(
        "stock entha undi", "entha undi", "stock chudu", "undha", "unada", "stock",
        "available", "how much stock", "check stock", "ennalu unnayi",
        "ఎంత ఉంది", "స్టాక్ ఎంత ఉంది", "స్టాక్ చూడు", "ఉందా", "సరుకు ఉందా"
    )

    val COMPLETE_BILL_PHRASES = listOf(
        "complete bill", "finish bill", "bill cheyyi", "bill cheyi", "checkout",
        "save bill", "print bill", "bill ivvu", "బిల్లు చెయ్యి", "బిల్లు పూర్తి చెయ్యి"
    )

    val CLEAR_BILL_PHRASES = listOf(
        "clear bill", "cancel bill", "clear cart", "reset bill", "remove all",
        "బిల్లు క్లియర్ చెయ్యి", "బిల్లు రద్దు చెయ్యి"
    )

    val ADD_VERBS = listOf(
        "add", "veseyi", "vei", "karchu", "rayi", "put", "vesuko", "add cheyyi", "add cheyi",
        "చెయ్యి", "వేసెయ్యి", "వేయి", "రాయి", "జతచేయి"
    )
}
