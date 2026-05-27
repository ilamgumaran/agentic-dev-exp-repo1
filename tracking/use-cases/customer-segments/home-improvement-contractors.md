# Customer Segment: Home Improvement Contractors

## Persona

**Who**: Licensed contractors (general, electrical, plumbing, HVAC) purchasing materials for active job sites. They buy in bulk, know exact product specs, and need items fast. They search by part number, brand, and technical specification — not by browsing.

**Search behavior**: Precise, technical queries. Uses brand names, model numbers, dimensions, and trade-specific jargon. Expects exact matches to rank first, followed by compatible alternatives. Rarely misspells because they know the products. Time-critical — wrong results cost job site downtime.

**What matters for ranking**:
- Exact SKU/model number match → highest priority
- Brand match → high priority
- Technical spec match (gauge, voltage, BTU, PSI) → high priority
- Category match → medium priority
- Semantic similarity → low priority (contractors know what they want)

## Scoring Weight Guidance

| Signal | Weight | Rationale |
|--------|--------|-----------|
| Token match (BM25) | 0.80 | Contractors search with precise terms |
| Semantic match | 0.10 | Minimal — they don't search conceptually |
| Field: sku/model | 3.0x | Exact part number is the #1 signal |
| Field: brand | 2.0x | Brand loyalty is strong in trades |
| Field: title | 2.0x | Product name carries key specs |
| Field: specs | 1.5x | Technical specifications matter |
| Field: category | 1.0x | Baseline grouping |
| Field: description | 0.5x | Marketing copy is noise for contractors |

## Top Keywords

| # | Query | Intent | Expected Top Result |
|---|-------|--------|---------------------|
| 1 | `romex 12/2 250ft` | Exact product: 12-gauge NM-B wire, 250ft roll | NM-B 12/2 250ft Romex |
| 2 | `3/4 copper coupling` | Plumbing fitting by size and material | 3/4" Copper Coupling C×C |
| 3 | `dewalt dcd999 drill` | Specific drill model by brand | DeWalt DCD999B FlexVolt Hammer Drill |
| 4 | `pvc schedule 40 2 inch` | Pipe by type, schedule, and diameter | 2" Schedule 40 PVC Pipe 10ft |
| 5 | `200 amp main breaker panel` | Electrical panel by amperage and type | Square D 200A Main Breaker Panel |
| 6 | `r-30 insulation batts` | Insulation by R-value and form | Owens Corning R-30 Fiberglass Batts |
| 7 | `1/2 inch drywall 4x8` | Drywall by thickness and sheet size | 1/2" × 4' × 8' Gypsum Drywall |
| 8 | `ridgid propress jaws` | Tool accessory by brand and type | RIDGID ProPress Jaw Kit 1/2"-2" |
| 9 | `treated 2x6 16ft` | Lumber by treatment, dimension, and length | Pressure Treated 2×6×16 #2 Southern Pine |
| 10 | `hilti tapcon screws 1/4` | Concrete fastener by brand and size | Hilti Kwik-Con II+ 1/4" × 3-1/4" |
| 11 | `hvac flex duct 6 inch` | HVAC duct by type and diameter | 6" Insulated Flexible Duct 25ft |
| 12 | `hubbell gfci 20a` | Electrical device by brand and rating | Hubbell GFCI Receptacle 20A 125V |
| 13 | `unistrut 10ft` | Structural channel by length | Unistrut P1000T 1-5/8" × 10ft Channel |
| 14 | `makita circular saw blade 7 1/4` | Saw blade by brand and size | Makita 7-1/4" 24T Framing Blade |
| 15 | `fernco coupling 4 inch` | Pipe coupling by brand and size | Fernco 4" Flexible PVC Coupling |

## Product Samples

```json
[
  {
    "id": "HI-001",
    "fields": {
      "sku": "ROM-122-250",
      "title": "Romex 12/2 NM-B Wire with Ground 250ft",
      "brand": "Southwire",
      "category": "Electrical > Wire & Cable",
      "specs": "12 AWG, 2 conductors, solid copper, 600V, NM-B rated, with ground",
      "description": "Professional-grade non-metallic sheathed cable for residential and commercial wiring. UL listed, meets NEC requirements.",
      "price": "189.99",
      "unit": "roll"
    }
  },
  {
    "id": "HI-002",
    "fields": {
      "sku": "COP-34-COUP",
      "title": "3/4 Inch Copper Coupling C×C with Stop",
      "brand": "Nibco",
      "category": "Plumbing > Fittings > Copper",
      "specs": "3/4\" nominal, C×C (sweat), with stop,?"lead-free, ASTM B75",
      "description": "Wrot copper pressure fitting for joining two pieces of copper tubing. Lead-free compliant for potable water systems.",
      "price": "2.49",
      "unit": "each"
    }
  },
  {
    "id": "HI-003",
    "fields": {
      "sku": "DCD999B",
      "title": "DeWalt DCD999B 20V MAX FlexVolt Advantage 1/2 in Hammer Drill",
      "brand": "DeWalt",
      "category": "Tools > Power Tools > Drills",
      "specs": "20V MAX, FlexVolt Advantage, 1/2\" chuck, 3-speed, 2250 RPM, brushless motor",
      "description": "High-performance hammer drill with FlexVolt Advantage technology. 3-speed transmission for application-specific performance. Tool-only, battery not included.",
      "price": "199.00",
      "unit": "each"
    }
  },
  {
    "id": "HI-004",
    "fields": {
      "sku": "PVC-S40-2-10",
      "title": "2 Inch Schedule 40 PVC Pipe 10ft Plain End",
      "brand": "Charlotte Pipe",
      "category": "Plumbing > Pipe > PVC",
      "specs": "2\" nominal, Schedule 40, 10ft length, plain end, ASTM D1785, NSF-pw",
      "description": "Schedule 40 PVC pressure pipe for cold water supply and DWV applications. Solvent weld connections.",
      "price": "8.97",
      "unit": "length"
    }
  },
  {
    "id": "HI-005",
    "fields": {
      "sku": "SQD-HOM2040M200PC",
      "title": "Square D Homeline 200 Amp 20-Space 40-Circuit Main Breaker Panel",
      "brand": "Square D",
      "category": "Electrical > Panels & Breakers",
      "specs": "200A main breaker, 20 spaces, 40 circuits, 120/240V, 1-phase, NEMA 1 indoor",
      "description": "Homeline load center with factory-installed 200A main breaker. Includes cover and 20 tandem breaker positions. UL listed.",
      "price": "189.00",
      "unit": "each"
    }
  },
  {
    "id": "HI-006",
    "fields": {
      "sku": "OC-R30-BATT",
      "title": "Owens Corning R-30 Kraft Faced Fiberglass Insulation Batts 10 in x 24 in",
      "brand": "Owens Corning",
      "category": "Building Materials > Insulation",
      "specs": "R-30, 10\" thick, 24\" wide, kraft faced, fiberglass, 48 sq ft per bag",
      "description": "EcoTouch insulation with PureFiber Technology. For floors and ceilings. Meets ASTM C665 Type I. Pink color for easy identification.",
      "price": "62.98",
      "unit": "bag"
    }
  },
  {
    "id": "HI-007",
    "fields": {
      "sku": "DW-12-4X8",
      "title": "1/2 in x 4 ft x 8 ft Regular Gypsum Drywall Board",
      "brand": "Gold Bond",
      "category": "Building Materials > Drywall",
      "specs": "1/2\" thick, 4' × 8' sheet, regular core, tapered edge, ASTM C1396",
      "description": "Standard interior drywall panel for walls and ceilings. Tapered long edges for smooth finishing. Fire-rated when properly installed.",
      "price": "12.48",
      "unit": "sheet"
    }
  },
  {
    "id": "HI-008",
    "fields": {
      "sku": "PT-2X6-16",
      "title": "Pressure Treated 2×6×16 #2 Southern Yellow Pine",
      "brand": "Yellowstone Lumber",
      "category": "Lumber > Pressure Treated",
      "specs": "2×6 nominal (1.5\" × 5.5\" actual), 16ft length, #2 grade, SYP, ground contact rated",
      "description": "Ground contact pressure treated lumber for decks, fences, and outdoor structures. Treated with copper azole preservative. Meets AWPA UC4A.",
      "price": "18.97",
      "unit": "piece"
    }
  },
  {
    "id": "HI-009",
    "fields": {
      "sku": "RIGID-PP-JAWS",
      "title": "RIDGID Standard Series ProPress Jaw Kit 1/2 to 2 Inch",
      "brand": "RIDGID",
      "category": "Tools > Plumbing Tools > Press Tools",
      "specs": "Jaw sizes: 1/2\", 3/4\", 1\", 1-1/4\", 1-1/2\", 2\". Compatible with RIDGID RP 350 and RP 351",
      "description": "Complete jaw kit for copper and stainless steel press connections. Includes carrying case. For use with RIDGID compact press tools.",
      "price": "1,899.00",
      "unit": "kit"
    }
  },
  {
    "id": "HI-010",
    "fields": {
      "sku": "HIL-KWIK-14-314",
      "title": "Hilti Kwik-Con II+ 1/4 in x 3-1/4 in Phillips Flat Head Concrete Screw",
      "brand": "Hilti",
      "category": "Fasteners > Concrete Anchors",
      "specs": "1/4\" diameter, 3-1/4\" length, Phillips flat head, blue Climaseal coating, carbon steel",
      "description": "High-performance concrete and masonry screw anchor. Pre-assembled with drill bit. ICC-ES certified. For use in concrete, block, and brick.",
      "price": "32.50",
      "unit": "box of 100"
    }
  },
  {
    "id": "HI-011",
    "fields": {
      "sku": "FLEX-6-25-INS",
      "title": "6 Inch Insulated Flexible HVAC Duct 25ft",
      "brand": "Master Flow",
      "category": "HVAC > Ductwork > Flexible",
      "specs": "6\" diameter, 25ft length, R-6 insulation, inner core: galvanized steel wire helix with polyester film, UL 181",
      "description": "Insulated flexible duct for heating and cooling air distribution. R-6 fiberglass insulation with metalized jacket. UL 181 Class 1 air duct.",
      "price": "49.98",
      "unit": "piece"
    }
  },
  {
    "id": "HI-012",
    "fields": {
      "sku": "HUB-GF20-WH",
      "title": "Hubbell Wiring 20A 125V GFCI Receptacle White",
      "brand": "Hubbell",
      "category": "Electrical > Devices > Receptacles",
      "specs": "20A, 125V, NEMA 5-20R, self-testing GFCI, back and side wired, white, UL 943",
      "description": "Commercial-grade auto-monitoring GFCI receptacle. LED indicator for ground fault protection status. Tamper-resistant. Wallplate not included.",
      "price": "24.99",
      "unit": "each"
    }
  }
]
```

## Expected Ranking Behavior

### Query: "romex 12/2 250ft"
1. HI-001 (exact match — Romex 12/2 wire 250ft) — score >0.9
2. All others — score <0.2 (no relevance)

### Query: "dewalt drill"
1. HI-003 (DeWalt DCD999B drill) — score >0.7
2. All others — score <0.15

### Query: "200 amp panel"
1. HI-005 (Square D 200A panel) — score >0.7
2. All others — score <0.15

### Query: "insulation"
1. HI-006 (R-30 insulation batts) — score >0.5
2. HI-011 (insulated flex duct — partial match) — score 0.1-0.3
3. All others — score <0.1

### Query: "2 inch pipe"
1. HI-004 (2" PVC pipe) — score >0.6
2. HI-009 (ProPress jaws — mentions 2" but is a tool) — score 0.1-0.2
3. All others — score <0.1
