# Customer Segment: Email Search Users

## Persona

**Who**: Knowledge workers searching their email inbox or corporate email archive. They need to find specific conversations, attachments, meeting invites, or action items buried in thousands of messages. They remember fragments — a name, a date range, a topic, or a phrase — but rarely the exact subject line.

**Search behavior**: Fragment-based recall — they remember pieces of an email but not the whole thing. Searches by sender name, partial subject, topic keyword, or time reference ("last week", "from marketing"). Heavy use of concept-based queries ("budget approval email", "that link John sent"). Short queries (2-3 words). Often searching under time pressure.

**What matters for ranking**:
- Sender/from match → very high priority
- Subject line match → high priority
- Body content match → medium priority
- Recency → strong signal (recent emails rank higher for tie-breaking)
- Semantic match → medium-high (conceptual recall is common)
- Attachment name → medium priority

## Scoring Weight Guidance

| Signal | Weight | Rationale |
|--------|--------|-----------|
| Token match (BM25) | 0.55 | Mix of exact recall and conceptual search |
| Semantic match | 0.35 | "that budget thing" needs concept matching |
| Field: from | 2.5x | Sender is a strong memory anchor |
| Field: subject | 2.5x | Subject line is the primary identifier |
| Field: body | 1.0x | Body text is large but contains the details |
| Field: to | 1.0x | Recipients can narrow results |
| Field: attachment | 1.5x | Attachment names are often searched |
| Field: date | 0.5x | Recency as tiebreaker, not primary signal |

## Top Keywords

| # | Query | Intent | Expected Top Result |
|---|-------|--------|---------------------|
| 1 | `budget approval sarah` | Approval email from Sarah | Email from Sarah about budget |
| 2 | `quarterly report pdf` | Email with attached report | Email with quarterly report PDF |
| 3 | `meeting reschedule friday` | Schedule change notification | Meeting move notification |
| 4 | `invoice acme corp` | Billing from specific vendor | Invoice email from/about Acme |
| 5 | `project timeline update` | Status update on project | Project status email |
| 6 | `password reset link` | Account recovery email | Password reset notification |
| 7 | `offer letter` | Job offer or proposal | Employment offer or deal proposal |
| 8 | `flight confirmation booking` | Travel itinerary | Flight booking confirmation |
| 9 | `contract renewal terms` | Agreement renewal details | Contract renewal email |
| 10 | `team lunch thursday` | Social/logistics coordination | Lunch invitation email |

## Product Samples (Email Snippets)

```json
[
  {
    "id": "EM-001",
    "fields": {
      "from": "sarah.chen@company.com",
      "to": "you@company.com, finance-team@company.com",
      "subject": "RE: Q4 Marketing Budget — Approved",
      "date": "2025-11-14",
      "body": "Hi team, Good news — the Q4 marketing budget of $250,000 has been approved by the CFO. Please see the attached breakdown by campaign. Digital advertising gets $120K, events $80K, and content production $50K. Let me know if you have any questions. Sarah",
      "attachment": "Q4_Marketing_Budget_Approved.xlsx"
    }
  },
  {
    "id": "EM-002",
    "fields": {
      "from": "reports@analytics.company.com",
      "to": "leadership@company.com",
      "subject": "Q3 2025 Quarterly Business Review Report",
      "date": "2025-10-05",
      "body": "Please find attached the Q3 2025 Quarterly Business Review. Key highlights: Revenue up 12% YoY, customer churn decreased to 2.1%, NPS improved from 42 to 51. Full details in the attached PDF report and supporting data in the spreadsheet.",
      "attachment": "Q3_2025_QBR_Report.pdf, Q3_2025_QBR_Data.xlsx"
    }
  },
  {
    "id": "EM-003",
    "fields": {
      "from": "john.martinez@company.com",
      "to": "you@company.com",
      "subject": "Can we move Friday's design review to 2pm?",
      "date": "2025-11-12",
      "body": "Hey, I have a conflict at 10am on Friday. Can we reschedule the design review to 2pm instead? Same room (Maple 3B). I'll update the calendar invite if that works for you. Thanks, John",
      "attachment": ""
    }
  },
  {
    "id": "EM-004",
    "fields": {
      "from": "billing@acmecorp.com",
      "to": "accounts-payable@company.com",
      "subject": "Invoice #INV-2025-4421 — Acme Corporation — November Services",
      "date": "2025-11-01",
      "body": "Please find attached Invoice #INV-2025-4421 for consulting services rendered in October 2025. Amount due: $18,500.00. Payment terms: Net 30. Please remit payment by December 1, 2025. For questions, contact billing@acmecorp.com.",
      "attachment": "INV-2025-4421_AcmeCorp.pdf"
    }
  },
  {
    "id": "EM-005",
    "fields": {
      "from": "pm@company.com",
      "to": "engineering-all@company.com",
      "subject": "Project Atlas — Updated Timeline and Milestones",
      "date": "2025-11-10",
      "body": "Hi everyone, Following last week's planning session, here's the updated Project Atlas timeline. Phase 1 (API foundation) moves from Nov 15 to Dec 1 due to the authentication dependency. Phase 2 (frontend) stays on track for Jan 15. Phase 3 (beta launch) pushed to Feb 28. See the attached Gantt chart for the full breakdown. Blocker: We're still waiting on the SSO integration from the identity team.",
      "attachment": "Project_Atlas_Timeline_v3.pdf"
    }
  },
  {
    "id": "EM-006",
    "fields": {
      "from": "no-reply@accounts.service.com",
      "to": "you@personal.com",
      "subject": "Password Reset Request — Action Required",
      "date": "2025-11-13",
      "body": "We received a request to reset the password for your account. Click the link below to create a new password. This link expires in 24 hours. If you did not request this, please ignore this email. Your account security is important to us.",
      "attachment": ""
    }
  },
  {
    "id": "EM-007",
    "fields": {
      "from": "hr@company.com",
      "to": "candidate@email.com",
      "subject": "Offer Letter — Senior Software Engineer Position",
      "date": "2025-10-20",
      "body": "Dear Candidate, We are delighted to extend an offer for the Senior Software Engineer position on the Platform team. Base salary: $185,000. Annual bonus target: 15%. Equity: 2,000 RSUs vesting over 4 years. Start date: December 2, 2025. Please review the attached formal offer letter and return the signed copy by November 3, 2025.",
      "attachment": "Offer_Letter_Senior_SWE.pdf"
    }
  },
  {
    "id": "EM-008",
    "fields": {
      "from": "travel-confirm@airline.com",
      "to": "you@personal.com",
      "subject": "Booking Confirmation — Flight ATL→SFO Dec 15",
      "date": "2025-11-08",
      "body": "Your flight has been booked! Confirmation code: XKRF42. Departure: Dec 15, 2025 at 8:15am from Atlanta (ATL), Terminal S. Arrival: 11:20am San Francisco (SFO), Terminal 2. Return: Dec 19, 2025 at 6:30pm from SFO. Seat: 14A (window). Checked bag included. Mobile boarding pass available 24 hours before departure.",
      "attachment": "Flight_Itinerary_Dec2025.pdf"
    }
  },
  {
    "id": "EM-009",
    "fields": {
      "from": "legal@vendor.com",
      "to": "procurement@company.com",
      "subject": "RE: Service Agreement Renewal — Revised Terms for 2026",
      "date": "2025-11-11",
      "body": "Hi, Attached is the revised service agreement for 2026 renewal. Key changes from the current contract: price increase of 8% reflecting expanded scope, SLA uptime guarantee increased to 99.95%, and a new data processing addendum for GDPR compliance. Contract term: 2 years with annual opt-out after Year 1. Please review and let us know if you'd like to schedule a call to discuss.",
      "attachment": "Service_Agreement_2026_Revised.pdf, DPA_Addendum.pdf"
    }
  },
  {
    "id": "EM-010",
    "fields": {
      "from": "lisa.wong@company.com",
      "to": "engineering-team@company.com",
      "subject": "Team lunch this Thursday — Taqueria El Sol, 12:30pm",
      "date": "2025-11-11",
      "body": "Hey team! 🌮 Sprint 14 is wrapping up so let's celebrate with a team lunch this Thursday. I made a reservation at Taqueria El Sol for 12:30pm — it's the place on 5th Street that got great reviews. Lunch is on the team budget. Reply here or DM me if you have dietary restrictions. See you there! Lisa",
      "attachment": ""
    }
  }
]
```

## Expected Ranking Behavior

### Query: "budget approval sarah"
1. EM-001 (from sarah.chen, subject "Budget — Approved") — score >0.9
2. All others — score <0.1

### Query: "quarterly report pdf"
1. EM-002 (quarterly business review with PDF attachment) — score >0.85
2. All others — score <0.1

### Query: "invoice acme corp"
1. EM-004 (invoice from Acme Corporation) — score >0.9
2. All others — score <0.05

### Query: "project timeline update"
1. EM-005 (Project Atlas updated timeline) — score >0.8
2. All others — score <0.1

### Query: "offer letter"
1. EM-007 (offer letter for Senior SWE) — score >0.85
2. EM-009 (contract renewal — not an "offer letter" but a "revised terms") — score 0.1-0.2

### Query: "flight confirmation"
1. EM-008 (flight booking confirmation) — score >0.85
2. All others — score <0.05

### Query: "team lunch thursday"
1. EM-010 (team lunch this Thursday) — score >0.9
2. All others — score <0.05
