# Customer Segment: Financial Analysts

## Persona

**Who**: Analysts at banks, hedge funds, and corporations searching through financial statements, SEC filings, earnings reports, and transaction records. They need to find specific records matching exact financial terms, ticker symbols, reporting periods, and regulatory references.

**Search behavior**: Extremely precise. Uses ticker symbols (AAPL, MSFT), financial jargon (EBITDA, amortization, covenant), reporting periods (Q3 2025, FY2024), and regulatory references (ASC 606, IFRS 16). Zero tolerance for irrelevant results. Often searches within a corpus of pre-filtered documents.

**What matters for ranking**:
- Exact term match → critical (ticker symbols, accounting terms are unambiguous)
- Date/period match → very high priority
- Financial metric match → high priority
- Semantic match → low priority (financial terms have precise meanings)

## Scoring Weight Guidance

| Signal | Weight | Rationale |
|--------|--------|-----------|
| Token match (BM25) | 0.85 | Financial search is precision-driven |
| Semantic match | 0.05 | Minimal — "revenue" does not mean "income" in accounting |
| Field: ticker | 3.0x | Definitive company identifier |
| Field: title | 2.0x | Document name carries key info |
| Field: period | 2.5x | Reporting period is critical |
| Field: content | 1.5x | Body text contains the data |
| Field: category | 1.0x | Document type (10-K, 10-Q, etc) |
| Field: metrics | 2.0x | Financial figures and KPIs |

## Top Keywords

| # | Query | Intent | Expected Top Result |
|---|-------|--------|---------------------|
| 1 | `AAPL revenue Q3 2025` | Apple quarterly revenue | AAPL Q3 2025 earnings report |
| 2 | `MSFT operating income FY2025` | Microsoft annual profit | MSFT FY2025 annual report |
| 3 | `TSLA gross margin trend` | Tesla profitability analysis | TSLA margin data across periods |
| 4 | `goodwill impairment 10-K` | Specific accounting event in annual filing | 10-K with goodwill writedown |
| 5 | `debt covenant violation` | Loan compliance issue | Filing with covenant breach |
| 6 | `revenue recognition ASC 606` | Accounting standard application | Filing discussing ASC 606 |
| 7 | `NVDA data center segment` | Nvidia segment reporting | NVDA segment breakdown |
| 8 | `accounts receivable aging` | AR quality analysis | A/R aging schedule |
| 9 | `stock buyback authorization` | Share repurchase program | Board authorization filing |
| 10 | `AMZN AWS operating margin` | Amazon cloud segment profit | AMZN segment report |

## Product Samples (Financial Records)

```json
[
  {
    "id": "FIN-001",
    "fields": {
      "title": "Apple Inc. Quarterly Report (10-Q) Q3 FY2025",
      "ticker": "AAPL",
      "period": "Q3 FY2025 (Apr-Jun 2025)",
      "category": "SEC Filing > 10-Q",
      "content": "Net revenue for the three months ended June 28, 2025 was $85.8 billion, an increase of 5% compared to $81.8 billion in Q3 FY2024. Products revenue was $63.4 billion. Services revenue reached $22.4 billion, up 12% year-over-year. Gross margin was 46.3%.",
      "metrics": "Revenue: $85.8B, Products: $63.4B, Services: $22.4B, Gross Margin: 46.3%"
    }
  },
  {
    "id": "FIN-002",
    "fields": {
      "title": "Microsoft Corporation Annual Report (10-K) FY2025",
      "ticker": "MSFT",
      "period": "FY2025 (Jul 2024 - Jun 2025)",
      "category": "SEC Filing > 10-K",
      "content": "Total revenue was $254.2 billion, an increase of 16% from FY2024. Operating income was $118.5 billion, up 22%. Intelligent Cloud segment revenue was $110.4 billion driven by Azure growth of 32%. Revenue recognition follows ASC 606 for all contract types.",
      "metrics": "Revenue: $254.2B, Operating Income: $118.5B, Azure Growth: 32%, Cloud: $110.4B"
    }
  },
  {
    "id": "FIN-003",
    "fields": {
      "title": "Tesla Inc. Quarterly Report (10-Q) Q2 2025",
      "ticker": "TSLA",
      "period": "Q2 2025 (Apr-Jun 2025)",
      "category": "SEC Filing > 10-Q",
      "content": "Automotive revenue was $21.3 billion with automotive gross margin of 18.7%, up from 17.4% in Q1 2025. Energy generation and storage revenue grew 52% to $3.1 billion. Total gross margin improved to 19.8%. Vehicle deliveries were 485,000 units.",
      "metrics": "Auto Revenue: $21.3B, Auto Gross Margin: 18.7%, Energy: $3.1B, Deliveries: 485K"
    }
  },
  {
    "id": "FIN-004",
    "fields": {
      "title": "Acme Corp Annual Report (10-K) FY2024 — Goodwill Impairment",
      "ticker": "ACME",
      "period": "FY2024",
      "category": "SEC Filing > 10-K",
      "content": "The Company recorded a goodwill impairment charge of $142 million in the Consumer Products reporting unit during Q4 FY2024. The impairment was triggered by revised revenue forecasts and increased discount rates. After the impairment, remaining goodwill on the consolidated balance sheet is $890 million across three reporting units.",
      "metrics": "Goodwill Impairment: $142M, Remaining Goodwill: $890M"
    }
  },
  {
    "id": "FIN-005",
    "fields": {
      "title": "GlobalBank Corp Credit Agreement Amendment — Covenant Modification",
      "ticker": "GBC",
      "period": "Q1 2025",
      "category": "Credit Agreement > Amendment",
      "content": "The Company entered into Amendment No. 3 to its Revolving Credit Facility dated March 15, 2025. The amendment modifies the maximum net leverage ratio covenant from 3.5x to 4.0x EBITDA through Q4 2025, reverting to 3.5x thereafter. The Company was in violation of the original 3.5x covenant as of December 31, 2024 with actual leverage of 3.72x.",
      "metrics": "Leverage Ratio: 3.72x (actual), Covenant: 3.5x→4.0x (amended), Facility: Revolving Credit"
    }
  },
  {
    "id": "FIN-006",
    "fields": {
      "title": "NVIDIA Corporation Quarterly Report (10-Q) Q2 FY2026",
      "ticker": "NVDA",
      "period": "Q2 FY2026 (May-Jul 2025)",
      "category": "SEC Filing > 10-Q",
      "content": "Data Center segment revenue was $30.0 billion, up 154% year-over-year, driven by demand for Hopper and Blackwell GPU architectures for AI training and inference. Data Center operating margin was 72.1%. Gaming segment revenue was $3.3 billion, up 9%. Automotive revenue reached $410 million.",
      "metrics": "Data Center: $30.0B (+154%), DC Margin: 72.1%, Gaming: $3.3B, Auto: $410M"
    }
  },
  {
    "id": "FIN-007",
    "fields": {
      "title": "MidCo Industries Accounts Receivable Aging Schedule Q4 2024",
      "ticker": "MDCO",
      "period": "Q4 2024 (as of Dec 31, 2024)",
      "category": "Internal Report > A/R Aging",
      "content": "Total accounts receivable: $45.2 million. Current (0-30 days): $28.1M (62%). 31-60 days: $8.9M (20%). 61-90 days: $4.5M (10%). Over 90 days: $3.7M (8%). Allowance for doubtful accounts: $2.1M. Three customers represent 42% of total receivables. DSO increased from 38 to 44 days compared to Q3 2024.",
      "metrics": "Total A/R: $45.2M, Current: 62%, 90+ Days: 8%, Allowance: $2.1M, DSO: 44 days"
    }
  },
  {
    "id": "FIN-008",
    "fields": {
      "title": "TechGrowth Inc Board Resolution — Share Repurchase Authorization",
      "ticker": "TGRO",
      "period": "March 2025",
      "category": "Board Resolution > Capital Allocation",
      "content": "The Board of Directors authorized a new share repurchase program of up to $500 million of the Company's common stock. The program has no expiration date and replaces the prior authorization with $120 million remaining. Repurchases may be made through open market purchases, privately negotiated transactions, or structured programs.",
      "metrics": "Buyback Authorization: $500M, Prior Remaining: $120M, Expiration: None"
    }
  },
  {
    "id": "FIN-009",
    "fields": {
      "title": "Amazon.com Inc. Segment Report — AWS Q3 2025",
      "ticker": "AMZN",
      "period": "Q3 2025 (Jul-Sep 2025)",
      "category": "SEC Filing > 10-Q > Segment",
      "content": "Amazon Web Services segment net sales were $29.5 billion, an increase of 19% year-over-year. AWS operating income was $10.4 billion with operating margin of 35.3%, compared to 30.3% in Q3 2024. The improvement in operating margin reflects continued efficiency gains in infrastructure operations and higher-margin AI services.",
      "metrics": "AWS Revenue: $29.5B (+19%), AWS Operating Income: $10.4B, AWS Margin: 35.3%"
    }
  },
  {
    "id": "FIN-010",
    "fields": {
      "title": "SoftwareCo Inc Revenue Recognition Policy — ASC 606 Application",
      "ticker": "SWCO",
      "period": "FY2024",
      "category": "SEC Filing > 10-K > Accounting Policy",
      "content": "The Company recognizes revenue in accordance with ASC 606, Revenue from Contracts with Customers. SaaS subscription revenue is recognized ratably over the contract term. Professional services revenue is recognized over time using the input method based on labor hours incurred. The Company identified three performance obligations in bundled arrangements: software license, implementation services, and post-contract support.",
      "metrics": "Standard: ASC 606, Method: Ratable (SaaS) / Input (Services), Performance Obligations: 3"
    }
  }
]
```

## Expected Ranking Behavior

### Query: "AAPL revenue Q3 2025"
1. FIN-001 (AAPL Q3 FY2025 — exact ticker, period, and metric) — score >0.95
2. All others — score <0.1

### Query: "goodwill impairment 10-K"
1. FIN-004 (Acme Corp goodwill impairment in 10-K) — score >0.85
2. All others — score <0.1

### Query: "debt covenant violation"
1. FIN-005 (GlobalBank covenant modification — "violation" in content) — score >0.8
2. All others — score <0.05

### Query: "NVDA data center segment"
1. FIN-006 (NVIDIA Data Center segment) — score >0.9
2. All others — score <0.05

### Query: "revenue recognition ASC 606"
1. FIN-010 (ASC 606 application policy) — score >0.85
2. FIN-002 (mentions ASC 606 briefly) — score 0.2-0.4
