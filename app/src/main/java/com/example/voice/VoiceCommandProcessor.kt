package com.example.voice

import com.example.data.local.ProductEntity
import java.util.Locale

class VoiceCommandProcessor {

    /**
     * Parses a spoken or typed command in English, Telugu, or Tanglish against the current products.
     */
    fun processCommand(
        transcript: String,
        availableProducts: List<ProductEntity>
    ): ParsedVoiceAction {
        val trimmed = transcript.trim()
        if (trimmed.isEmpty()) {
            return ParsedVoiceAction(
                rawTranscript = transcript,
                actionType = VoiceActionType.UNKNOWN,
                feedbackMessage = "No voice input detected / శబ్దం రాలేదు"
            )
        }

        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Check for Daily Sales intent
        if (TeluguGroceryLexicon.SALES_PHRASES.any { lower.contains(it) }) {
            return ParsedVoiceAction(
                rawTranscript = transcript,
                actionType = VoiceActionType.SHOW_DAILY_SALES,
                confidence = 0.95f,
                feedbackMessage = "Showing today's sales summary / ఈరోజు అమ్మకాలు"
            )
        }

        // 2. Check for Complete Bill intent
        if (TeluguGroceryLexicon.COMPLETE_BILL_PHRASES.any { lower.contains(it) }) {
            return ParsedVoiceAction(
                rawTranscript = transcript,
                actionType = VoiceActionType.COMPLETE_BILL,
                confidence = 0.95f,
                requiresConfirmation = true,
                confirmationPrompt = "Confirm and complete this bill? / బిల్లు పూర్తి చేయాలా?",
                feedbackMessage = "Ready to complete bill"
            )
        }

        // 3. Check for Clear Bill intent
        if (TeluguGroceryLexicon.CLEAR_BILL_PHRASES.any { lower.contains(it) }) {
            return ParsedVoiceAction(
                rawTranscript = transcript,
                actionType = VoiceActionType.CLEAR_BILL,
                confidence = 0.95f,
                requiresConfirmation = true,
                confirmationPrompt = "Clear all items from current bill? / బిల్లు మొత్తం తీసేయాలా?",
                feedbackMessage = "Clear current bill request"
            )
        }

        // 4. Check for Stock Query intent (e.g. "Oil stock entha undi?", "నూనె ఎంత ఉంది?")
        val isStockQuery = TeluguGroceryLexicon.STOCK_PHRASES.any { lower.contains(it) }

        // Tokenize sentence
        val cleanSentence = lower
            .replace(",", " ")
            .replace(".", " ")
            .replace("?", " ")
            .replace("!", " ")
            .replace("'", "")
            .replace("\"", "")

        val tokens = cleanSentence.split("\\s+".toRegex()).filter { it.isNotBlank() }

        // Extract Quantity & Unit
        val (extractedQty, extractedUnit) = extractQuantityAndUnit(tokens)

        // Match product against available items
        val matchResult = findBestProductMatches(tokens, availableProducts)

        if (isStockQuery) {
            return when {
                matchResult.bestMatch != null -> {
                    ParsedVoiceAction(
                        rawTranscript = transcript,
                        actionType = VoiceActionType.QUERY_STOCK,
                        targetProduct = matchResult.bestMatch,
                        candidateProducts = matchResult.candidates,
                        confidence = matchResult.confidence,
                        requiresConfirmation = matchResult.candidates.size > 1,
                        confirmationPrompt = if (matchResult.candidates.size > 1) "Multiple items match. Which product's stock do you want to check?" else "",
                        feedbackMessage = "Stock for ${matchResult.bestMatch.nameEnglish}: ${matchResult.bestMatch.stockQuantity} ${matchResult.bestMatch.unit}"
                    )
                }
                matchResult.candidates.isNotEmpty() -> {
                    ParsedVoiceAction(
                        rawTranscript = transcript,
                        actionType = VoiceActionType.QUERY_STOCK,
                        candidateProducts = matchResult.candidates,
                        confidence = 0.6f,
                        requiresConfirmation = true,
                        confirmationPrompt = "Select product to check stock / సరుకును ఎంచుకోండి:",
                        feedbackMessage = "Select product for stock lookup"
                    )
                }
                else -> {
                    ParsedVoiceAction(
                        rawTranscript = transcript,
                        actionType = VoiceActionType.QUERY_STOCK,
                        feedbackMessage = "Could not identify product for stock query: \"$transcript\""
                    )
                }
            }
        }

        // 5. Default action is ADD_ITEM
        return when {
            matchResult.bestMatch != null && matchResult.candidates.size <= 1 -> {
                val product = matchResult.bestMatch
                ParsedVoiceAction(
                    rawTranscript = transcript,
                    actionType = VoiceActionType.ADD_ITEM,
                    targetProduct = product,
                    candidateProducts = listOf(product),
                    extractedQuantity = extractedQty,
                    extractedUnit = extractedUnit ?: product.unit,
                    confidence = matchResult.confidence,
                    requiresConfirmation = false, // High confidence, direct add with undo toast
                    feedbackMessage = "Added ${formatQuantity(extractedQty)} ${extractedUnit ?: product.unit} of ${product.nameEnglish}"
                )
            }
            matchResult.candidates.isNotEmpty() -> {
                // Ambiguous match (e.g. "oil" matches Sunflower Oil and Groundnut Oil)
                ParsedVoiceAction(
                    rawTranscript = transcript,
                    actionType = VoiceActionType.ADD_ITEM,
                    candidateProducts = matchResult.candidates,
                    extractedQuantity = extractedQty,
                    extractedUnit = extractedUnit,
                    confidence = 0.65f,
                    requiresConfirmation = true,
                    confirmationPrompt = "Which product did you mean for \"$transcript\"? / ఏ సరుకు కావాలో ఎంచుకోండి:",
                    feedbackMessage = "Please select the product to add"
                )
            }
            else -> {
                ParsedVoiceAction(
                    rawTranscript = transcript,
                    actionType = VoiceActionType.UNKNOWN,
                    feedbackMessage = "Unrecognized command: \"$transcript\". Try: \"Add 2 kg rice\" or \"రెండు కిలోల బియ్యం\""
                )
            }
        }
    }

    private data class MatchScoreResult(
        val bestMatch: ProductEntity?,
        val candidates: List<ProductEntity>,
        val confidence: Float
    )

    private fun findBestProductMatches(
        tokens: List<String>,
        products: List<ProductEntity>
    ): MatchScoreResult {
        val nonVerbTokens = tokens.filter { token ->
            !TeluguGroceryLexicon.ADD_VERBS.contains(token) &&
            !TeluguGroceryLexicon.STOCK_PHRASES.any { it.contains(token) } &&
            !TeluguGroceryLexicon.NUMBER_WORDS.containsKey(token) &&
            !TeluguGroceryLexicon.UNIT_WORDS.containsKey(token) &&
            token.toDoubleOrNull() == null &&
            token !in listOf("of", "the", "a", "an", "cheyyi", "cheyi", "undi", "entha", "lo", "ki")
        }

        val searchPhrase = nonVerbTokens.joinToString(" ")
        val scoredProducts = mutableListOf<Pair<ProductEntity, Float>>()

        for (product in products) {
            val eng = product.nameEnglish.lowercase(Locale.ROOT)
            val tel = product.nameTelugu.lowercase(Locale.ROOT)
            val aliases = product.aliases.lowercase(Locale.ROOT).split(",").map { it.trim() }

            var maxScore = 0.0f

            // Full phrase exact match
            if (searchPhrase.isNotBlank()) {
                if (eng == searchPhrase || tel == searchPhrase) maxScore = maxOf(maxScore, 1.0f)
                else if (aliases.any { it == searchPhrase }) maxScore = maxOf(maxScore, 0.95f)
                else if (eng.contains(searchPhrase) || tel.contains(searchPhrase)) maxScore = maxOf(maxScore, 0.85f)
                else if (aliases.any { it.contains(searchPhrase) || searchPhrase.contains(it) }) maxScore = maxOf(maxScore, 0.8f)
            }

            // Token by token match
            for (token in nonVerbTokens) {
                if (token.length < 2) continue
                if (eng.split(" ").any { it == token } || tel.split(" ").any { it == token }) {
                    maxScore = maxOf(maxScore, 0.9f)
                } else if (aliases.any { it.equals(token, ignoreCase = true) }) {
                    maxScore = maxOf(maxScore, 0.85f)
                } else if (eng.contains(token) || tel.contains(token) || aliases.any { it.contains(token) }) {
                    maxScore = maxOf(maxScore, 0.65f)
                }
            }

            if (maxScore >= 0.60f) {
                scoredProducts.add(Pair(product, maxScore))
            }
        }

        // Sort descending by match score
        scoredProducts.sortByDescending { it.second }

        val best = scoredProducts.firstOrNull()?.first
        val bestScore = scoredProducts.firstOrNull()?.second ?: 0.0f
        val candidates = scoredProducts.take(4).map { it.first }

        return MatchScoreResult(
            bestMatch = if (candidates.size == 1 || (scoredProducts.size > 1 && (scoredProducts[0].second - scoredProducts[1].second) >= 0.2f)) best else null,
            candidates = candidates,
            confidence = bestScore
        )
    }

    private fun extractQuantityAndUnit(tokens: List<String>): Pair<Double, String?> {
        var quantity = 1.0
        var foundQuantity = false
        var unit: String? = null

        // Check for numeric strings e.g. "2", "2.5", "500"
        for (i in tokens.indices) {
            val token = tokens[i]
            val numeric = token.toDoubleOrNull()
            if (numeric != null && !foundQuantity) {
                quantity = numeric
                foundQuantity = true
                // Check if adjacent next token is a unit
                if (i + 1 < tokens.size) {
                    val nextToken = tokens[i + 1]
                    if (TeluguGroceryLexicon.UNIT_WORDS.containsKey(nextToken)) {
                        unit = TeluguGroceryLexicon.UNIT_WORDS[nextToken]
                    }
                }
            }
        }

        // Check for number words in lexicon
        if (!foundQuantity) {
            for (token in tokens) {
                if (TeluguGroceryLexicon.NUMBER_WORDS.containsKey(token)) {
                    quantity = TeluguGroceryLexicon.NUMBER_WORDS[token] ?: 1.0
                    foundQuantity = true
                    break
                }
            }
        }

        // Check for unit if not yet set
        if (unit == null) {
            for (token in tokens) {
                if (TeluguGroceryLexicon.UNIT_WORDS.containsKey(token)) {
                    unit = TeluguGroceryLexicon.UNIT_WORDS[token]
                    break
                }
            }
        }

        // Gram normalization: if quantity is >= 50 and unit is 'g', keep as grams or convert as needed
        return Pair(quantity, unit)
    }

    private fun formatQuantity(qty: Double): String {
        return if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
    }
}
