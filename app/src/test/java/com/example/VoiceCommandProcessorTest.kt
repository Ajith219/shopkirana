package com.example

import com.example.data.local.ProductEntity
import com.example.voice.VoiceActionType
import com.example.voice.VoiceCommandProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VoiceCommandProcessorTest {

    private lateinit var processor: VoiceCommandProcessor
    private lateinit var sampleProducts: List<ProductEntity>

    @Before
    fun setUp() {
        processor = VoiceCommandProcessor()
        sampleProducts = listOf(
            ProductEntity(
                id = "p-rice",
                nameEnglish = "Sona Masuri Rice",
                nameTelugu = "సోనా మసూరి బియ్యం",
                aliases = "rice, biyyam, chawal, sonamasuri",
                category = "Grains",
                unit = "kg",
                pricePerUnit = 58.0,
                stockQuantity = 50.0
            ),
            ProductEntity(
                id = "p-sunflower-oil",
                nameEnglish = "Sunflower Oil",
                nameTelugu = "సన్‌ఫ్లవర్ నూనె",
                aliases = "oil, sunflower oil, noone, cooking oil",
                category = "Oils",
                unit = "l",
                pricePerUnit = 135.0,
                stockQuantity = 20.0
            ),
            ProductEntity(
                id = "p-groundnut-oil",
                nameEnglish = "Groundnut Oil",
                nameTelugu = "వేరుశనగ నూనె",
                aliases = "oil, groundnut oil, palli noone",
                category = "Oils",
                unit = "l",
                pricePerUnit = 180.0,
                stockQuantity = 15.0
            ),
            ProductEntity(
                id = "p-sugar",
                nameEnglish = "Sugar",
                nameTelugu = "పంచదార",
                aliases = "sugar, panchadara, cheeni",
                category = "Essentials",
                unit = "kg",
                pricePerUnit = 42.0,
                stockQuantity = 30.0
            ),
            ProductEntity(
                id = "p-milk",
                nameEnglish = "Whole Milk",
                nameTelugu = "పాలు",
                aliases = "milk, paalu, packet milk",
                category = "Dairy",
                unit = "pkt",
                pricePerUnit = 32.0,
                stockQuantity = 10.0
            )
        )
    }

    @Test
    fun testEnglishAddRiceCommand() {
        val result = processor.processCommand("Add two kilos of rice", sampleProducts)
        assertEquals(VoiceActionType.ADD_ITEM, result.actionType)
        assertEquals(2.0, result.extractedQuantity, 0.01)
        assertEquals("kg", result.extractedUnit)
        assertNotNull(result.targetProduct)
        assertEquals("Sona Masuri Rice", result.targetProduct?.nameEnglish)
    }

    @Test
    fun testTeluguAddRiceCommand() {
        val result = processor.processCommand("రెండు కిలోల బియ్యం add cheyyi", sampleProducts)
        assertEquals(VoiceActionType.ADD_ITEM, result.actionType)
        assertEquals(2.0, result.extractedQuantity, 0.01)
        assertNotNull(result.targetProduct)
        assertEquals("Sona Masuri Rice", result.targetProduct?.nameEnglish)
    }

    @Test
    fun testStockQueryCommand() {
        val result = processor.processCommand("Oil stock entha undi?", sampleProducts)
        assertEquals(VoiceActionType.QUERY_STOCK, result.actionType)
        assertTrue(result.candidateProducts.isNotEmpty())
    }

    @Test
    fun testShowTodaySalesCommand() {
        val result = processor.processCommand("Show today's sales", sampleProducts)
        assertEquals(VoiceActionType.SHOW_DAILY_SALES, result.actionType)
    }

    @Test
    fun testCompleteBillCommand() {
        val result = processor.processCommand("bill cheyyi", sampleProducts)
        assertEquals(VoiceActionType.COMPLETE_BILL, result.actionType)
        assertTrue(result.requiresConfirmation)
    }
}
