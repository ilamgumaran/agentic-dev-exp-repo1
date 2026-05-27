# Customer Segment: DIY Home Improvers

## Persona

**Who**: Homeowners tackling weekend projects — bathroom remodels, deck builds, painting, garden work. Mix of experience levels from first-timers watching YouTube tutorials to seasoned hobbyists. They buy smaller quantities and need guidance. Often don't know exact product names.

**Search behavior**: Descriptive, conceptual queries using everyday language, not trade jargon. Searches by project ("bathroom tile"), by problem ("leaky faucet"), or by vague description ("wood stain dark brown"). Frequently misspells or uses generic terms. Browses results more than contractors do.

**What matters for ranking**:
- Semantic match → high priority (they describe what they need, not the product name)
- Category match → high priority
- Title keyword match → medium-high priority
- Description match → medium priority (they actually read descriptions)
- Brand match → low priority (less brand-loyal than contractors)
- SKU match → very low priority (they don't know part numbers)

## Scoring Weight Guidance

| Signal | Weight | Rationale |
|--------|--------|-----------|
| Token match (BM25) | 0.50 | They use approximate language |
| Semantic match | 0.40 | Concept matching matters ("wood finish" = "stain") |
| Field: title | 2.5x | Product name is their primary scan target |
| Field: category | 2.0x | Category helps them narrow down |
| Field: description | 1.5x | They read descriptions for guidance |
| Field: specs | 0.5x | Most don't understand spec details |
| Field: brand | 0.5x | Less brand-driven |
| Field: sku | 0.0x | Never search by SKU |

## Top Keywords

| # | Query | Intent | Expected Top Result |
|---|-------|--------|---------------------|
| 1 | `bathroom floor tile` | Tile for bathroom remodel | Ceramic floor tile |
| 2 | `deck stain waterproof` | Protective coating for outdoor wood | Exterior wood stain/sealant |
| 3 | `easy install kitchen faucet` | Replacement faucet for DIY install | Single-handle pull-down kitchen faucet |
| 4 | `outdoor string lights patio` | Decorative patio lighting | LED outdoor string lights |
| 5 | `paint for cabinets white` | Cabinet refinishing paint | Cabinet & trim enamel paint white |
| 6 | `garden raised bed kit` | Pre-made raised garden bed | Cedar raised garden bed kit |
| 7 | `motion sensor porch light` | Exterior security/convenience lighting | LED motion-activated outdoor wall light |
| 8 | `how to fix running toilet` | Toilet repair parts | Toilet flapper and fill valve kit |
| 9 | `closet organizer shelves` | Storage organization system | Closet shelving system |
| 10 | `vinyl plank flooring waterproof` | DIY-friendly flooring | Luxury vinyl plank click-lock flooring |
| 11 | `fence panels wood 6ft` | Privacy fence sections | 6' × 8' cedar dog-ear fence panel |
| 12 | `smart thermostat easy install` | Programmable home thermostat | WiFi smart thermostat |
| 13 | `shower head rain style` | Bathroom fixture upgrade | Rain shower head ceiling mount |
| 14 | `concrete crack filler driveway` | Driveway repair product | Concrete crack sealant |
| 15 | `cordless drill starter kit` | First power tool purchase | Cordless drill/driver kit with battery |

## Product Samples

```json
[
  {
    "id": "DIY-001",
    "fields": {
      "title": "Italian Porcelain Floor Tile 12x24 Marble Look Grey",
      "category": "Flooring > Tile > Porcelain",
      "description": "Beautiful marble-look porcelain tile perfect for bathrooms, kitchens, and entryways. Easy to clean and waterproof. No sealing required. Rectified edges for minimal grout lines. Suitable for floor and wall installation.",
      "specs": "12\" × 24\", porcelain, PEI 4, frost resistant, 8mm thick, 15.5 sq ft per box",
      "price": "3.49",
      "unit": "sq ft"
    }
  },
  {
    "id": "DIY-002",
    "fields": {
      "title": "Behr Premium Semi-Transparent Weatherproofing Wood Stain and Sealer",
      "brand": "Behr",
      "category": "Paint > Exterior Stains > Wood Stain",
      "description": "All-in-one wood stain and sealer for decks, fences, siding, and outdoor furniture. Resists UV damage, mildew, and water. Easy soap and water cleanup. Dries in 4 hours. Covers up to 250 sq ft per gallon.",
      "specs": "1 gallon, semi-transparent, water-based, low VOC, 250 sq ft coverage",
      "price": "42.98",
      "unit": "gallon"
    }
  },
  {
    "id": "DIY-003",
    "fields": {
      "title": "Moen Adler Single-Handle Pull-Down Kitchen Faucet Chrome",
      "brand": "Moen",
      "category": "Kitchen > Faucets > Pull-Down",
      "description": "Easy DIY install kitchen faucet with Duralock quick-connect system — no tools needed for water line connections. Pull-down spray head with two spray modes. Spot-resistant chrome finish. Includes deck plate for 1 or 3-hole installation.",
      "specs": "Single-handle, pull-down spray, chrome finish, 1.5 GPM, ADA compliant",
      "price": "159.00",
      "unit": "each"
    }
  },
  {
    "id": "DIY-004",
    "fields": {
      "title": "Hampton Bay 24-Light 48ft LED Indoor/Outdoor String Lights",
      "brand": "Hampton Bay",
      "category": "Lighting > Outdoor > String Lights",
      "description": "Create the perfect patio ambiance with these shatterproof LED string lights. 48 feet with 24 warm white bulbs. Weatherproof for year-round outdoor use. Connectable end-to-end up to 3 strands. Energy-efficient LED bulbs last up to 15,000 hours.",
      "specs": "48ft, 24 bulbs, LED, 2700K warm white, weatherproof IP65, 12W total",
      "price": "29.98",
      "unit": "strand"
    }
  },
  {
    "id": "DIY-005",
    "fields": {
      "title": "Benjamin Moore Advance Interior Paint Satin White for Cabinets and Trim",
      "brand": "Benjamin Moore",
      "category": "Paint > Interior > Cabinet Paint",
      "description": "Professional-quality paint specially designed for kitchen cabinets, bathroom vanities, trim, and doors. Self-leveling formula delivers a smooth factory-like finish with a brush or roller. Excellent adhesion, no priming needed on most surfaces. Low odor and easy cleanup.",
      "specs": "1 quart, satin finish, alkyd formula, 400 sq ft coverage, dries in 16 hours",
      "price": "32.99",
      "unit": "quart"
    }
  },
  {
    "id": "DIY-006",
    "fields": {
      "title": "Greenes Cedar Raised Garden Bed Kit 4ft × 8ft × 10.5in",
      "brand": "Greenes Fence",
      "category": "Outdoors > Garden Center > Raised Beds",
      "description": "Easy-to-assemble raised garden bed made from untreated cedar. No tools required — stackable dovetail joints snap together in minutes. Cedar naturally resists rot and insects. Perfect for vegetables, herbs, and flowers. Expandable with additional kits.",
      "specs": "4' × 8' × 10.5\" tall, cedar wood, no hardware needed, 26.9 cu ft soil capacity",
      "price": "129.00",
      "unit": "kit"
    }
  },
  {
    "id": "DIY-007",
    "fields": {
      "title": "Ring Floodlight Cam Wired Plus with Motion-Activated LED",
      "brand": "Ring",
      "category": "Smart Home > Security > Outdoor Cameras",
      "description": "HD security camera with ultra-bright motion-activated LED floodlights for your porch, driveway, or garage. Two-way talk, siren alarm, and color night vision. Works with Alexa for voice-activated monitoring. Hardwired installation replaces existing outdoor light.",
      "specs": "1080p HD, 2000 lumens, 140° motion zone, WiFi, hardwired",
      "price": "199.99",
      "unit": "each"
    }
  },
  {
    "id": "DIY-008",
    "fields": {
      "title": "Fluidmaster 400CRP14 Universal Toilet Repair Kit",
      "brand": "Fluidmaster",
      "category": "Plumbing > Toilet Parts > Repair Kits",
      "description": "Everything you need to fix a running toilet in one box. Includes the 400A fill valve, 2\" flapper, and tank-to-bowl hardware. Fits most standard toilets. Easy DIY installation — no plumber needed. Step-by-step instructions and video link included.",
      "specs": "Universal fit, includes fill valve + flapper + bolts, fits 2\" flush valves",
      "price": "14.98",
      "unit": "kit"
    }
  },
  {
    "id": "DIY-009",
    "fields": {
      "title": "ClosetMaid ShelfTrack 5-8ft Adjustable Closet Organizer Kit White",
      "brand": "ClosetMaid",
      "category": "Storage > Closet Organization > Shelving Systems",
      "description": "Customizable closet shelving system that adjusts from 5 to 8 feet wide. Includes shelves, hang rods, and all mounting hardware. Tool-free adjustment after installation. Double your closet storage space in about 2 hours.",
      "specs": "5-8ft adjustable width, 4 shelves, 2 hang rods, white epoxy-coated steel, 80 lb per shelf",
      "price": "119.00",
      "unit": "kit"
    }
  },
  {
    "id": "DIY-010",
    "fields": {
      "title": "Lifeproof Sterling Oak Waterproof Luxury Vinyl Plank Flooring",
      "brand": "Lifeproof",
      "category": "Flooring > Vinyl > Luxury Vinyl Plank",
      "description": "100% waterproof vinyl plank flooring with realistic oak wood look. Drop-and-lock installation — no glue, no nails, no mess. Perfect for kitchens, bathrooms, basements, and any room. Scratch-resistant, pet-proof, and kid-proof. Attached underlayment for comfort underfoot.",
      "specs": "7.1\" × 47.6\" planks, 8.7mm thick, attached cork underlayment, 20.06 sq ft per case",
      "price": "3.69",
      "unit": "sq ft"
    }
  }
]
```

## Expected Ranking Behavior

### Query: "bathroom floor tile"
1. DIY-001 (porcelain floor tile — "bathrooms" in description, "floor tile" in title) — score >0.8
2. DIY-010 (vinyl plank — "bathrooms" mentioned, "flooring") — score 0.3-0.5
3. All others — score <0.15

### Query: "fix running toilet"
1. DIY-008 (toilet repair kit — "fix a running toilet" in description) — score >0.8
2. All others — score <0.1

### Query: "waterproof floor"
1. DIY-010 (waterproof vinyl plank flooring) — score >0.7
2. DIY-001 (porcelain tile — "waterproof" in description) — score 0.3-0.5
3. DIY-002 (weatherproofing stain — partial semantic match) — score 0.1-0.2

### Query: "outdoor lights"
1. DIY-004 (outdoor string lights) — score >0.7
2. DIY-007 (Ring floodlight cam — outdoor light replacement) — score 0.4-0.6
3. All others — score <0.1

### Query: "easy install kitchen"
1. DIY-003 (kitchen faucet — "easy DIY install" in description) — score >0.6
2. DIY-005 (cabinet paint — kitchen cabinets) — score 0.2-0.4
3. All others — score <0.1
