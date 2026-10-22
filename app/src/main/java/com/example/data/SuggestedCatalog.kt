package com.example.data

data class SuggestedItem(
    val id: String,
    val nameEnglish: String,
    val nameTelugu: String,
    val aliases: String,
    val category: String,
    val unit: String,
    val suggestedPrice: Double,
    val defaultStock: Double
)

object SuggestedCatalog {
    val items: List<SuggestedItem> = listOf(
        // Grains & Flours
        SuggestedItem("sug-rice-01", "Sona Masuri Rice", "సోనా మసూరి బియ్యం", "rice, biyyam, chawal, sonamasuri", "Grains", "kg", 56.0, 50.0),
        SuggestedItem("sug-basmati-02", "Basmati Rice", "బాస్మతి బియ్యం", "basmati, biryani rice, basmati biyyam", "Grains", "kg", 110.0, 25.0),
        SuggestedItem("sug-atta-03", "Aashirvaad Atta", "గోధుమ పిండి", "atta, wheat flour, godhuma pindi", "Grains", "kg", 50.0, 40.0),
        SuggestedItem("sug-maida-04", "Maida Flour", "మైదా పిండి", "maida, all purpose flour, maida pindi", "Grains", "kg", 42.0, 20.0),
        SuggestedItem("sug-rava-05", "Bombay Rava (Sooji)", "బొంబాయి రవ్వ", "rava, sooji, bombay rava, upma rava", "Grains", "kg", 44.0, 20.0),

        // Pulses & Dals
        SuggestedItem("sug-toordal-06", "Toor Dal (Kandi Pappu)", "కందిపప్పు", "toor dal, kandi pappu, dal, arhar", "Pulses", "kg", 145.0, 30.0),
        SuggestedItem("sug-moongdal-07", "Moong Dal (Pesara Pappu)", "పెసరపప్పు", "moong dal, pesara pappu, moong", "Pulses", "kg", 120.0, 25.0),
        SuggestedItem("sug-uraddal-08", "Urad Dal (Minapappu)", "మినపప్పు", "urad dal, minapappu, minamulu", "Pulses", "kg", 130.0, 20.0),
        SuggestedItem("sug-chana-09", "Bengal Gram (Senagapappu)", "శనగపప్పు", "senagapappu, chana dal, senagalu", "Pulses", "kg", 90.0, 25.0),

        // Oils & Ghee
        SuggestedItem("sug-sunflower-10", "Freedom Sunflower Oil", "సన్‌ఫ్లవర్ నూనె", "oil, sunflower oil, noone, cooking oil", "Oils", "l", 130.0, 24.0),
        SuggestedItem("sug-groundnut-11", "Groundnut Oil", "వేరుశనగ నూనె", "groundnut oil, palli noone, peanut oil", "Oils", "l", 175.0, 15.0),
        SuggestedItem("sug-ghee-12", "Pure Cow Ghee", "ఆవు నెయ్యి", "ghee, neyyi, cow ghee", "Oils", "kg", 550.0, 10.0),

        // Essentials & Sweeteners
        SuggestedItem("sug-sugar-13", "Sugar (Panchadara)", "పంచదార", "sugar, panchadara, cheeni, sakhara", "Essentials", "kg", 42.0, 40.0),
        SuggestedItem("sug-jaggery-14", "Organic Jaggery (Bellam)", "బెల్లం", "jaggery, bellam, gur", "Essentials", "kg", 58.0, 20.0),
        SuggestedItem("sug-salt-15", "Tata Salt", "టాటా ఉప్పు", "salt, uppu, tata salt, namak", "Essentials", "pkt", 25.0, 50.0),

        // Spices & Powders
        SuggestedItem("sug-chilli-16", "Guntur Chilli Powder", "కారం పొడి", "chilli powder, karam, mirchi powder", "Spices", "kg", 220.0, 15.0),
        SuggestedItem("sug-turmeric-17", "Turmeric Powder (Pasupu)", "పసుపు పొడి", "turmeric, pasupu, haldi", "Spices", "kg", 160.0, 15.0),
        SuggestedItem("sug-mustard-18", "Mustard Seeds (Aavalu)", "ఆవాలు", "mustard, aavalu, rai", "Spices", "kg", 110.0, 10.0),
        SuggestedItem("sug-cumin-19", "Cumin Seeds (Jeelakarra)", "జీలకర్ర", "cumin, jeelakarra, jeera", "Spices", "kg", 320.0, 10.0),

        // Dairy & Beverages
        SuggestedItem("sug-milk-20", "Heritage Whole Milk", "హెరిటేజ్ పాలు", "milk, paalu, packet milk", "Dairy", "pkt", 32.0, 30.0),
        SuggestedItem("sug-curd-21", "Fresh Curd (Perugu)", "పెరుగు", "curd, perugu, dahi", "Dairy", "pkt", 35.0, 20.0),
        SuggestedItem("sug-tea-22", "Red Label Tea Powder", "రెడ్ లేబుల్ టీ పొడి", "tea, tea podi, chai, red label", "Beverages", "pkt", 95.0, 25.0),
        SuggestedItem("sug-coffee-23", "Bru Instant Coffee", "బ్రూ కాఫీ పొడి", "coffee, bru, coffee podi", "Beverages", "pkt", 80.0, 20.0),

        // Vegetables
        SuggestedItem("sug-onion-24", "Fresh Onions", "ఉల్లిపాయలు", "onions, ullipayalu, ulligadda, pyaz", "Vegetables", "kg", 35.0, 50.0),
        SuggestedItem("sug-potato-25", "Potatoes (Alu)", "బంగాళాదుంపలు", "potatoes, potato, alu, bangaladumpalu", "Vegetables", "kg", 30.0, 40.0),
        SuggestedItem("sug-ginger-garlic-26", "Ginger Garlic Paste", "అల్లం వెల్లుల్లి పేస్ట్", "ginger garlic, allam vellulli paste", "Vegetables", "pkt", 20.0, 30.0),

        // Household & Soap
        SuggestedItem("sug-rin-27", "Rin Detergent Bar", "రిన్ సబ్బు", "rin, rin soap, sabbu, laundry soap", "Household", "piece", 10.0, 60.0),
        SuggestedItem("sug-surf-28", "Surf Excel Powder", "సర్ఫ్ ఎక్సెల్ పొడి", "surf excel, detergent powder, surf", "Household", "kg", 140.0, 20.0),
        SuggestedItem("sug-lifebuoy-29", "Lifebuoy Soap", "లైఫ్‌బాయ్ సబ్బు", "lifebuoy, bath soap, sabbu", "Household", "piece", 28.0, 40.0)
    )
}
