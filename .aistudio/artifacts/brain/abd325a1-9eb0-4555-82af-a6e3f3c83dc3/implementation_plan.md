# KiranaVoice - Voice Billing & Inventory Management

An offline-first Android application designed for Indian kirana (grocery) shopkeepers to rapidly bill items, query stock, and record transactions using natural voice commands in English, Telugu, and Tanglish (mixed Telugu-English), backed by a persistent Room database ready for Supabase cloud sync.

## User Review & Critical Decisions

> [!IMPORTANT]
> Based on your Phase 1 selections, here are the confirmed architectural decisions:
> - **Native Android with Jetpack Compose**: High-performance, tactile UI with direct Android speech-to-text hardware access and instant zero-latency feedback.
> - **Simpler Starting Point for Telugu/English Voice Commands**: Robust, deterministic bilingual phonetic parser (matching Telugu script, romanized Tanglish, and English) with explicit confidence checks, plus Gemini AI natural language command understanding fallback.
> - **Offline-First Room Database with Supabase Sync Architecture**: Room SQLite provides instant local transactional guarantees and zero-network dependence for checkout counters, with sync metadata columns (`supabase_id`, `sync_status`, `updated_at`) prepared for cloud sync.

---

## 1. Overview & Core Concept

- **What It Does**: Enables grocery store owners to run their entire counter by voice or tap: saying *"Add two kilos of rice"*, *"రెండు కిలోల బియ్యం add cheyyi"*, or *"Oil stock entha undi?"*. It matches spoken names to inventory, displays a live billing table in Indian Rupees (₹), prevents overselling, prints/shares summary bills, and preserves all sales and stock changes safely in local storage.
- **Target Audience / Persona**: Local Indian kirana and provision store owners who need ultra-fast, single-handed or hands-free operation during peak customer rushes without complex software training.
- **Key Value**: Speeds up checkout by up to 3x, eliminates manual calculation errors, provides instant Telugu and English auditory/visual confirmation, and prevents inventory discrepancies through atomic database transactions.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Quick Billing Flow**:
   - The shopkeeper taps the large floating microphone or activates continuous voice mode.
   - The shopkeeper speaks in Telugu, English, or Tanglish (e.g., *"3 packets of milk"* or *"రెండు కేజీల పంచదార"*).
   - An immediate voice recognition preview card appears showing the heard transcript, detected intent, item match, quantity, and unit price.
   - If ambiguous (e.g., multiple oil brands or unclear quantity), a quick confirmation modal lets the shopkeeper tap the intended product in one click.
   - Item is staged in the active bill with subtotal. Total updates instantly in ₹.
   - Tapping **"Complete Bill & Deduct Stock"** triggers an atomic stock deduction transaction, generates a bill receipt with invoice number, and clears the cart for the next customer.
2. **Stock Query & Inventory Management**:
   - Spoken queries like *"Sunflower oil stock entha undi?"* immediately show an overlay card with current stock, reorder threshold warning, and quick restock buttons.
   - Inventory tab allows adding, editing, and searching products (with Telugu & English aliases, categories like Grains, Spices, Dairy, Oils, and unit types: kg, g, l, ml, packets, pcs).
3. **Daily Sales & Bill History**:
   - Spoken command *"Show today's sales"* or navigating to the Sales tab displays total daily revenue (₹), total bills created, top-selling items, and a searchable chronological bill archive with shareable receipt cards.

### Visual Identity & Theme
- **Aesthetic Direction**: Warm Utilitarian & Modern Kirana. High-contrast typography, large touch targets (minimum 56dp for main counter actions), clear Indian Rupee (₹) currency badges, and green/amber stock status indicators.
- **Color Palette**:
  - `Primary`: Deep Forest Green (`#1B5E20`) — signifies commerce, prosperity, and Indian retail tradition.
  - `Secondary Accent`: Warm Marigold / Amber (`#F57F17`) — high-visibility badges and voice listening state.
  - `Background`: Soft Cream / Crisp Surface (`#F8F9FA` / `#FFFFFF`) with high-contrast text (`#1C1B1F`).
  - `Stock Status`: Emerald Green (In Stock), Amber/Orange (Low Stock < 5), Crimson Red (Out of Stock).
- **Typography & Hierarchy**:
  - Display Font: High-legibility sans-serif bold for currency and numbers (`₹1,240.00`).
  - Bilingual Labels: Side-by-side English & Telugu cues (e.g., *Rice / బియ్యం*, *Sugar / పంచదార*) so anyone at the counter can operate it comfortably.
- **Interactive Feedback**:
  - Active audio wave pulsation when listening to speech.
  - Tactile haptic click and subtle chime sound on recognized voice item.
  - Safety dialog requiring explicit confirmation before clearing an active bill or deleting inventory.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Hybrid Bilingual Voice Engine (Rule-Based + AI)**
  - *Chosen Approach*: On-device rule-based tokenizer and phonetic dictionary for common Indian grocery terms in Telugu script, Tanglish transliterations (e.g., *biyyam, panchadara, kandi pappu, noone, paalu*), and English, backed by Google SpeechRecognizer and server-side Gemini parsing for complex multi-item sentences.
  - *Why*: Delivers instantaneous zero-latency response for everyday kirana items even when internet connectivity is spotty, while retaining natural language flexibility.
  - *Alternatives Considered*: Pure cloud-only NLP (too slow for peak billing queues, fails without internet) or pure English-only regex (unusable for Telugu kirana shopkeepers).
- **Decision 2: Offline-First Room Architecture with Supabase Schema Parity**
  - *Chosen Approach*: SQLite Room Database with entities designed to map 1:1 to Supabase PostgreSQL tables (`products`, `bills`, `bill_items`, `inventory_logs`), including UUID keys, `sync_status` (`SYNCED`, `PENDING_INSERT`, `PENDING_UPDATE`), and ISO timestamps.
  - *Why*: Guarantees zero transaction loss during network dropouts; enables immediate offline usability while providing a clean sync gateway to Supabase.
- **Decision 3: Strict Stock Deduction Safeguards**
  - *Chosen Approach*: Stock is reserved and deducted inside an `@Transaction` Room routine only when the bill is completed. If item quantity exceeds stock, the app prevents completion and prompts the user with the exact shortage.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                       Jetpack Compose UI                    │
│  ┌─────────────────┐  ┌──────────────────┐  ┌────────────┐  │
│  │  BillingScreen  │  │  InventoryScreen │  │ SalesScreen│  │
│  │ (Voice Bar, Cart│  │ (Stock List, Add │  │(Daily Stats│  │
│  │  & Quick Dial)  │  │  & Low Stock)    │  │ & Receipts)│  │
│  └────────┬────────┘  └────────┬─────────┘  └─────┬──────┘  │
└───────────┼────────────────────┼──────────────────┼─────────┘
            ▼                    ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│                 MainViewModel & BillingState                │
│  - Active Cart StateFlow         - Voice Recognition State  │
│  - Unconfirmed Action Modal      - Ambiguity Resolution     │
└───────────┬───────────────────────────────────────┬─────────┘
            ▼                                       ▼
┌───────────────────────────────┐     ┌───────────────────────┐
│ VoiceCommandProcessor         │     │  KiranaRepository     │
│ - Android SpeechRecognizer    │     │  - Atomic Checkout    │
│ - Telugu/Tanglish Tokenizer   │     │  - Inventory Mutator  │
│ - Telugu Grocery Dictionary   │     │  - Supabase Sync Hook │
└───────────────────────────────┘     └───────────┬───────────┘
                                                  ▼
                                      ┌───────────────────────┐
                                      │   Room Database       │
                                      │  - ProductEntity      │
                                      │  - BillEntity         │
                                      │  - BillItemEntity     │
                                      │  - SyncLogEntity      │
                                      └───────────────────────┘
```

### Data Model Entities (Room + Supabase Parity)
1. **ProductEntity**:
   - `id: String` (UUID)
   - `nameEnglish: String`, `nameTelugu: String`, `aliases: String` (comma-separated search terms e.g., "biyyam, chawal, sonamasuri")
   - `category: String` (Grains, Pulses, Dairy, Oils, Spices, Snacks, Essentials)
   - `unit: String` (kg, g, l, ml, pkt, piece)
   - `pricePerUnit: Double`
   - `stockQuantity: Double`
   - `minStockThreshold: Double`
   - `syncStatus: String`, `updatedAt: Long`
2. **BillEntity**:
   - `id: String` (UUID), `billNumber: String` (e.g., #INV-1001)
   - `totalAmount: Double`, `itemCount: Int`, `timestamp: Long`
   - `paymentMode: String` (CASH, UPI, PENDING)
   - `syncStatus: String`
3. **BillItemEntity**:
   - `id: String` (UUID), `billId: String`, `productId: String`
   - `productName: String`, `quantity: Double`, `unit: String`
   - `unitPrice: Double`, `subtotal: Double`

### Telugu & Tanglish Keyword & Speech Mapping
- **Quantities & Numbers**: 1 (ఒకటి / okati / one), 2 (రెండు / rendu / two), 3 (మూడు / moodu / three), 4 (నాలుగు / naalugu / four), 5 (ఐదు / aidu / five), etc.
- **Units**: కేజీ / kilo / kg, గ్రాములు / grams / g, లీటర్ / litre / l, ప్యాకెట్ / packet, డజన్ / dozen.
- **Action Verbs**: add cheyyi, add, veseyi, karchu, entha undi, chudu, bill cheyyi.
- **Common Items Pre-populated**: Sona Masuri Rice (బియ్యం), Toor Dal (కందిపప్పు), Sugar (పంచదార), Sunflower Oil (నూనె), Milk (పాలు), Tea Powder (టీ పొడి), Salt (ఉప్పు), Wheat Flour / Atta (గోధుమ పిండి), Onions (ఉల్లిపాయలు), Chilli Powder (కారం).
