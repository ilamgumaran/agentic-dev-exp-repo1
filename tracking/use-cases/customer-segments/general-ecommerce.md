# Customer Segment: General E-Commerce Shoppers

## Persona

**Who**: Everyday online shoppers browsing a general marketplace (think Amazon, Target, Walmart). They buy across many categories — electronics, clothing, home goods, personal care, toys. No domain expertise; they rely on product titles, ratings, and descriptions to make decisions.

**Search behavior**: Highly varied — from precise brand+product searches ("airpods pro 2") to vague intent-based queries ("gift for 10 year old boy"). Heavy use of adjectives and qualifiers ("best", "cheap", "lightweight", "wireless"). Often include use-case context ("laptop for college", "running shoes for flat feet"). Frequently use 2-4 word queries.

**What matters for ranking**:
- Title keyword match → highest priority
- Category relevance → high priority
- Description/feature match → medium priority for qualifier queries
- Brand match → medium priority (varies by category)
- Semantic match → medium-high (important for conceptual queries)
- Price → not a ranking signal but critical for "cheap"/"budget" qualifiers

## Scoring Weight Guidance

| Signal | Weight | Rationale |
|--------|--------|-----------|
| Token match (BM25) | 0.55 | Mix of precise and vague queries |
| Semantic match | 0.35 | Concept matching for intent queries |
| Field: title | 2.5x | Primary scan target |
| Field: category | 2.0x | Helps disambiguate |
| Field: description | 1.5x | Qualifiers found here |
| Field: brand | 1.0x | Moderate brand awareness |
| Field: features | 1.5x | Key for "wireless", "waterproof" etc |

## Top Keywords

| # | Query | Intent | Expected Top Result |
|---|-------|--------|---------------------|
| 1 | `wireless earbuds` | Audio product | Wireless Bluetooth earbuds |
| 2 | `yoga mat thick non slip` | Exercise equipment with specs | Premium yoga mat |
| 3 | `kids water bottle school` | Children's drinkware for school | Kids' insulated water bottle |
| 4 | `laptop stand adjustable` | Desk accessory | Adjustable laptop riser |
| 5 | `mens running shoes wide` | Athletic footwear with fit spec | Wide-fit running shoes |
| 6 | `portable phone charger` | Power bank | Portable battery pack |
| 7 | `stainless steel tumbler 30oz` | Insulated drinkware by size | 30oz insulated tumbler |
| 8 | `gift for mom birthday` | Gift exploration (vague) | Various (curated gift set) |
| 9 | `usb c hub multiport` | Computer accessory | USB-C multiport adapter |
| 10 | `dog bed large washable` | Pet supplies with specs | Large washable dog bed |
| 11 | `noise cancelling headphones` | Audio product with feature | ANC headphones |
| 12 | `resistance bands set` | Fitness equipment | Resistance band kit |
| 13 | `electric toothbrush` | Personal care | Rechargeable electric toothbrush |
| 14 | `backpack laptop 15 inch` | Bag with size requirement | Laptop backpack fits 15" |
| 15 | `led desk lamp dimmable` | Lighting with feature | Dimmable LED desk lamp |

## Product Samples

```json
[
  {
    "id": "GE-001",
    "fields": {
      "title": "SoundCore Liberty 4 NC Wireless Earbuds with Active Noise Cancelling",
      "brand": "Anker",
      "category": "Electronics > Audio > Earbuds",
      "description": "True wireless earbuds with adaptive active noise cancelling and Hi-Res audio. 50-hour total playtime with charging case. IPX4 water resistant for workouts. Multipoint connection pairs with two devices simultaneously. Touch controls and voice assistant support.",
      "features": "wireless, bluetooth 5.3, noise cancelling, waterproof, 50hr battery",
      "price": "99.99"
    }
  },
  {
    "id": "GE-002",
    "fields": {
      "title": "Gaiam Premium 6mm Thick Non-Slip Yoga Mat with Alignment Lines",
      "brand": "Gaiam",
      "category": "Sports > Yoga > Mats",
      "description": "Extra thick 6mm cushioning protects joints during floor exercises. Textured non-slip surface prevents sliding in any pose. Printed alignment lines help perfect your form. Lightweight and easy to carry with included yoga mat strap. Free from harmful phthalates.",
      "features": "6mm thick, non-slip texture, alignment guides, includes carry strap, 68\" × 24\"",
      "price": "29.98"
    }
  },
  {
    "id": "GE-003",
    "fields": {
      "title": "CamelBak Eddy+ Kids 14oz Water Bottle with Straw - Dinosaur Print",
      "brand": "CamelBak",
      "category": "Kitchen > Drinkware > Kids Water Bottles",
      "description": "Spill-proof kids water bottle perfect for school lunchboxes and sports. Bite valve and straw make drinking easy without tilting. BPA-free Tritan plastic is dishwasher safe. Fun dinosaur print that kids love. Fits most lunch boxes and cup holders.",
      "features": "14oz, spill-proof, BPA-free, dishwasher safe, straw cap",
      "price": "15.00"
    }
  },
  {
    "id": "GE-004",
    "fields": {
      "title": "BESIGN Adjustable Laptop Stand Aluminum Ergonomic Riser for Desk",
      "brand": "BESIGN",
      "category": "Office > Desk Accessories > Laptop Stands",
      "description": "Elevate your laptop to eye level to reduce neck strain. Six height settings from 2.5\" to 5.5\" above desk. Ventilated aluminum construction keeps laptop cool. Compatible with all laptops 10\" to 15.6\". Folds flat for travel. Rubber pads prevent scratching.",
      "features": "adjustable height, aluminum, foldable, fits 10-15.6 inch laptops, ventilated",
      "price": "25.99"
    }
  },
  {
    "id": "GE-005",
    "fields": {
      "title": "New Balance Fresh Foam X 880v14 Men's Running Shoes Wide (2E)",
      "brand": "New Balance",
      "category": "Shoes > Men's > Running",
      "description": "Plush, cushioned running shoe in wide width for everyday training. Fresh Foam X midsole delivers ultra-soft yet responsive ride mile after mile. Engineered mesh upper provides breathable comfort. Available in wide (2E) and extra-wide (4E) for flat feet and wide foot shapes.",
      "features": "wide fit 2E, Fresh Foam X cushioning, mesh upper, 10mm drop, road running",
      "price": "139.99"
    }
  },
  {
    "id": "GE-006",
    "fields": {
      "title": "Anker PowerCore 26800mAh Portable Charger with Dual USB",
      "brand": "Anker",
      "category": "Electronics > Power > Portable Chargers",
      "description": "High-capacity portable phone charger with 26800mAh battery — enough to charge an iPhone 6+ times or a Galaxy 5+ times. Dual USB-A output charges two devices simultaneously. PowerIQ technology detects device and delivers fastest possible charge. Compact design with LED battery indicator.",
      "features": "26800mAh, dual USB output, fast charging, LED indicator, pocket-size",
      "price": "59.99"
    }
  },
  {
    "id": "GE-007",
    "fields": {
      "title": "YETI Rambler 30oz Tumbler with MagSlider Lid Stainless Steel",
      "brand": "YETI",
      "category": "Kitchen > Drinkware > Tumblers",
      "description": "Double-wall vacuum insulated stainless steel tumbler keeps drinks ice-cold for hours or piping hot. 30oz capacity fits in most cup holders. MagSlider lid is shatter-resistant and splash-proof. 18/8 stainless steel, BPA-free. Dishwasher safe.",
      "features": "30oz, vacuum insulated, stainless steel, BPA-free, dishwasher safe, MagSlider lid",
      "price": "38.00"
    }
  },
  {
    "id": "GE-008",
    "fields": {
      "title": "USB C Hub Multiport Adapter 7-in-1: HDMI 4K, 100W PD, SD Card, USB 3.0",
      "brand": "Anker",
      "category": "Electronics > Accessories > USB Hubs",
      "description": "Expand your laptop's USB-C port into seven connections: 4K HDMI for external monitors, 100W Power Delivery for charging while connected, SD and microSD card readers, two USB 3.0 ports for peripherals, and one USB-C data port. Compatible with MacBook, iPad Pro, Surface, and all USB-C laptops.",
      "features": "7-in-1, USB-C, HDMI 4K, 100W PD, SD card reader, USB 3.0",
      "price": "35.99"
    }
  },
  {
    "id": "GE-009",
    "fields": {
      "title": "Bedsure Large Orthopedic Dog Bed Washable - Memory Foam Pet Bed",
      "brand": "Bedsure",
      "category": "Pet Supplies > Dogs > Beds",
      "description": "Orthopedic memory foam dog bed for large breeds up to 75 lbs. Egg-crate foam relieves joint pressure for senior and arthritic dogs. Removable, machine-washable cover with waterproof liner. Non-skid bottom prevents sliding on hard floors. 36\" × 27\" × 6\".",
      "features": "large, memory foam, machine washable, waterproof liner, non-skid, 36×27 inches",
      "price": "39.99"
    }
  },
  {
    "id": "GE-010",
    "fields": {
      "title": "Sony WH-1000XM5 Wireless Noise Cancelling Over-Ear Headphones",
      "brand": "Sony",
      "category": "Electronics > Audio > Headphones",
      "description": "Industry-leading noise cancellation with eight microphones and two processors. Exceptional sound quality with 30mm custom drivers. 30-hour battery life. Multipoint Bluetooth connects to two devices. Speak-to-Chat pauses music when you talk. Ultra-comfortable lightweight design at 250g.",
      "features": "noise cancelling, wireless, bluetooth, 30hr battery, multipoint, lightweight 250g",
      "price": "348.00"
    }
  },
  {
    "id": "GE-011",
    "fields": {
      "title": "Fit Simplify Resistance Loop Exercise Bands Set of 5",
      "brand": "Fit Simplify",
      "category": "Sports > Fitness > Resistance Bands",
      "description": "Five color-coded resistance bands from extra light to extra heavy for progressive training. Natural latex, snap-resistant construction. Use for physical therapy, yoga, pilates, stretching, and strength training. Includes carrying bag and instruction guide with 30+ exercises.",
      "features": "5 resistance levels, natural latex, includes carry bag and exercise guide",
      "price": "10.95"
    }
  },
  {
    "id": "GE-012",
    "fields": {
      "title": "Oral-B iO Series 5 Rechargeable Electric Toothbrush with AI",
      "brand": "Oral-B",
      "category": "Health > Oral Care > Electric Toothbrushes",
      "description": "Smart electric toothbrush with AI-powered brushing recognition that tracks which areas you've cleaned. Micro-vibration technology removes 100% more plaque than manual brushing. 5 cleaning modes including sensitive and whitening. Interactive display shows smiley face when you've brushed for the recommended 2 minutes.",
      "features": "rechargeable, AI tracking, 5 modes, pressure sensor, 4-day battery, includes 1 brush head",
      "price": "99.99"
    }
  }
]
```

## Expected Ranking Behavior

### Query: "wireless earbuds"
1. GE-001 (wireless earbuds — exact title match) — score >0.85
2. GE-010 (wireless headphones — close but different form factor) — score 0.3-0.5
3. All others — score <0.1

### Query: "noise cancelling headphones"
1. GE-010 (Sony noise cancelling headphones) — score >0.85
2. GE-001 (earbuds with noise cancelling) — score 0.3-0.5
3. All others — score <0.1

### Query: "laptop stand adjustable"
1. GE-004 (adjustable laptop stand) — score >0.85
2. All others — score <0.1

### Query: "dog bed large washable"
1. GE-009 (large washable dog bed) — score >0.9
2. All others — score <0.05

### Query: "usb c hub"
1. GE-008 (USB-C hub multiport) — score >0.85
2. All others — score <0.1

### Query: "gift for mom"
1. No strong match — semantic matching should surface items commonly gifted (tumbler, toothbrush, yoga mat) with low scores (0.1-0.3)
2. This tests graceful degradation when no document matches well
