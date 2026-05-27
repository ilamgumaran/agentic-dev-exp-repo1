# Customer Segments — Overview

This directory contains detailed re-ranking use cases for specific customer segments across different verticals. Each segment defines:

1. **Customer persona** — who they are, what they need
2. **Search behavior** — how they search, what signals matter
3. **Top keywords** — the 15-20 most common search queries for this segment
4. **Product samples** — 10-15 realistic product records with multi-field data
5. **Expected ranking behavior** — what "good" re-ranking looks like for this segment
6. **Scoring weight guidance** — how token vs semantic matching should be balanced

## Segments

| Segment | File | Industry |
|---------|------|----------|
| Home improvement contractors | [home-improvement-contractors.md](home-improvement-contractors.md) | Home improvement retail |
| DIY home improvers | [diy-home-improvers.md](diy-home-improvers.md) | Home improvement retail |
| Students shopping for books | [students-books.md](students-books.md) | Education / bookstore |
| General e-commerce shoppers | [general-ecommerce.md](general-ecommerce.md) | General retail |
| Financial analysts | [financial-analysts.md](financial-analysts.md) | Financial services |
| Email search users | [email-search.md](email-search.md) | Productivity / email |

## How Agents Use These

When implementing or testing the re-ranking library, use these customer segments to:

1. **Build test fixtures**: Each segment's product samples can be loaded as `Document` objects
2. **Define expected behavior**: The ranking expectations tell you what the correct output is
3. **Tune scoring weights**: Each segment has different weight guidance for token vs semantic matching
4. **Validate edge cases**: Cross-segment queries test the library's flexibility

## How These Connect to Specs

These segments inform but do not replace specs. The relationship:

```
Customer segment (WHY this ranking matters)
  └── Use case (WHAT feature enables it)
        └── Spec (HOW to implement it)
              └── Test data (concrete examples from segments)
```

The `specs/test-data/` directory contains machine-parseable versions of the product samples from these segments, formatted as JSON for direct use in tests.
