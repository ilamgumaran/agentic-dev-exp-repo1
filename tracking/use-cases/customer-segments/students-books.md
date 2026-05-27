# Customer Segment: Students Shopping for Books

## Persona

**Who**: High school and college students searching for textbooks, study guides, assigned readings, and reference materials. Budget-conscious, often searching for specific course-assigned titles but also browsing for supplementary materials. Mix of exact-title searches and topic-based exploration.

**Search behavior**: Searches by book title, author name, course subject, ISBN, or broad topic. Often copies exact title from a syllabus but may get it slightly wrong. Also searches conceptually ("intro to calculus textbook" or "organic chemistry study guide"). Cares about edition number. Frequently searches for cheaper alternatives to assigned texts.

**What matters for ranking**:
- Title exact match → highest priority (syllabus-driven)
- Author match → very high priority
- ISBN match → definitive (if provided)
- Subject/topic match → high priority for browsing
- Edition recency → medium priority
- Semantic match → medium priority for topic exploration
- Price → not a ranking signal but influences click-through

## Scoring Weight Guidance

| Signal | Weight | Rationale |
|--------|--------|-----------|
| Token match (BM25) | 0.60 | Title and author searches are precise |
| Semantic match | 0.30 | Topic exploration needs concept matching |
| Field: title | 3.0x | Book title is the #1 signal |
| Field: author | 2.5x | Students search by author name |
| Field: isbn | 3.0x | Definitive identifier when used |
| Field: subject | 2.0x | Course subject is a strong signal |
| Field: description | 1.0x | Helps for topic browsing |
| Field: edition | 1.0x | Edition matters for assignments |

## Top Keywords

| # | Query | Intent | Expected Top Result |
|---|-------|--------|---------------------|
| 1 | `calculus early transcendentals stewart` | Assigned calculus textbook | Stewart's Calculus: Early Transcendentals |
| 2 | `intro to psychology` | Introductory psych textbook | Intro to Psychology by various |
| 3 | `organic chemistry klein` | Specific organic chem textbook | Klein's Organic Chemistry |
| 4 | `978-0134753119` | ISBN search | Whichever book has this ISBN |
| 5 | `data structures and algorithms java` | CS course textbook | Data Structures & Algorithms in Java |
| 6 | `ap biology study guide` | AP exam prep book | AP Biology review/prep book |
| 7 | `microeconomics principles mankiw` | Econ textbook by author | Mankiw's Principles of Microeconomics |
| 8 | `american history since 1877` | History survey course textbook | US History textbook post-Civil War |
| 9 | `linear algebra 4th edition` | Math textbook by edition | Linear algebra textbook, 4th ed |
| 10 | `spanish textbook beginner` | Language course material | Beginner Spanish textbook |
| 11 | `college writing handbook` | English composition guide | Writing handbook/rhetoric |
| 12 | `anatomy and physiology lab manual` | Science lab companion | A&P lab manual |
| 13 | `python programming beginners` | Intro CS book | Beginner Python programming book |
| 14 | `mla format citation guide` | Writing reference | MLA Handbook |
| 15 | `sat prep book 2026` | Standardized test prep | SAT study guide current year |

## Product Samples

```json
[
  {
    "id": "BK-001",
    "fields": {
      "title": "Calculus: Early Transcendentals",
      "author": "James Stewart, Daniel Clegg, Saleem Watson",
      "isbn": "978-0357113516",
      "edition": "9th Edition",
      "subject": "Mathematics > Calculus",
      "description": "The leading calculus textbook for over 30 years. Clear explanations, patient examples, and carefully graded problem sets. Includes WebAssign access code for online homework. Covers single-variable and multivariable calculus.",
      "publisher": "Cengage Learning",
      "format": "Hardcover",
      "price": "249.95"
    }
  },
  {
    "id": "BK-002",
    "fields": {
      "title": "Psychology",
      "author": "David G. Myers, C. Nathan DeWall",
      "isbn": "978-1319132101",
      "edition": "13th Edition",
      "subject": "Social Sciences > Psychology > Introduction",
      "description": "The most widely used introductory psychology textbook worldwide. Engaging writing, up-to-date research, and comprehensive coverage of all major psychological perspectives. Includes LaunchPad access for interactive learning.",
      "publisher": "Worth Publishers",
      "format": "Hardcover",
      "price": "199.99"
    }
  },
  {
    "id": "BK-003",
    "fields": {
      "title": "Organic Chemistry",
      "author": "David R. Klein",
      "isbn": "978-1119659594",
      "edition": "4th Edition",
      "subject": "Science > Chemistry > Organic Chemistry",
      "description": "A student-friendly approach to organic chemistry that focuses on developing problem-solving skills. SkillBuilder examples guide students through the reasoning process. Includes WileyPLUS access for practice problems and 3D molecular models.",
      "publisher": "Wiley",
      "format": "Hardcover",
      "price": "219.95"
    }
  },
  {
    "id": "BK-004",
    "fields": {
      "title": "Data Structures and Algorithms in Java",
      "author": "Robert Lafore",
      "isbn": "978-0672324536",
      "edition": "2nd Edition",
      "subject": "Computer Science > Data Structures > Java",
      "description": "Learn data structures and algorithms using Java with clear visual explanations and runnable code examples. Covers arrays, linked lists, stacks, queues, trees, hash tables, heaps, graphs, and sorting algorithms. Workshop applets let you experiment with algorithms interactively.",
      "publisher": "Sams Publishing",
      "format": "Paperback",
      "price": "49.99"
    }
  },
  {
    "id": "BK-005",
    "fields": {
      "title": "Barron's AP Biology Premium Study Guide with 5 Practice Tests",
      "author": "Deborah T. Goldberg",
      "isbn": "978-1506288437",
      "edition": "8th Edition, 2026",
      "subject": "Test Prep > AP Exams > Biology",
      "description": "Comprehensive AP Biology review covering all four Big Ideas: Evolution, Energetics, Information Transfer, and Systems Interactions. Includes 5 full-length practice tests with detailed answer explanations. Study tips, key terminology, and laboratory review. Updated for the latest College Board AP Biology curriculum.",
      "publisher": "Barron's Educational Series",
      "format": "Paperback",
      "price": "24.99"
    }
  },
  {
    "id": "BK-006",
    "fields": {
      "title": "Principles of Microeconomics",
      "author": "N. Gregory Mankiw",
      "isbn": "978-0357133484",
      "edition": "10th Edition",
      "subject": "Business > Economics > Microeconomics",
      "description": "The bestselling microeconomics text known for its clear, concise explanations and real-world examples. Mankiw's engaging writing makes economic principles accessible to students with no prior economics background. Includes MindTap access for interactive assignments.",
      "publisher": "Cengage Learning",
      "format": "Paperback",
      "price": "129.95"
    }
  },
  {
    "id": "BK-007",
    "fields": {
      "title": "Give Me Liberty! An American History Volume 2: From 1877",
      "author": "Eric Foner",
      "isbn": "978-0393418248",
      "edition": "7th Edition",
      "subject": "History > United States > Post-Civil War",
      "description": "The leading survey of American history since 1877. Eric Foner's narrative is organized around the theme of freedom — its changing meanings, its evolving scope, and its persistent contradictions. Covers Reconstruction through the present day. Includes InQuizitive adaptive learning tool.",
      "publisher": "W.W. Norton",
      "format": "Paperback",
      "price": "69.95"
    }
  },
  {
    "id": "BK-008",
    "fields": {
      "title": "Introduction to Linear Algebra",
      "author": "Gilbert Strang",
      "isbn": "978-1733146678",
      "edition": "6th Edition",
      "subject": "Mathematics > Linear Algebra",
      "description": "Gilbert Strang's acclaimed introduction emphasizing understanding over memorization. Covers vectors, matrices, eigenvalues, SVD, and applications in data science and machine learning. Companion video lectures available free on MIT OpenCourseWare.",
      "publisher": "Wellesley-Cambridge Press",
      "format": "Hardcover",
      "price": "92.00"
    }
  },
  {
    "id": "BK-009",
    "fields": {
      "title": "Taller: An Intermediate Spanish Course",
      "author": "Jose A. Blanco",
      "isbn": "978-1543388619",
      "edition": "2nd Edition",
      "subject": "Languages > Spanish > Intermediate",
      "description": "Intermediate Spanish textbook with immersive cultural content from 21 Spanish-speaking countries. Integrates grammar, vocabulary, and communication skills through authentic readings, short films, and real-world tasks. Includes Supersite access for online practice.",
      "publisher": "Vista Higher Learning",
      "format": "Paperback",
      "price": "189.00"
    }
  },
  {
    "id": "BK-010",
    "fields": {
      "title": "Python Crash Course: A Hands-On, Project-Based Introduction to Programming",
      "author": "Eric Matthes",
      "isbn": "978-1718502703",
      "edition": "3rd Edition",
      "subject": "Computer Science > Programming > Python",
      "description": "The bestselling Python book for complete beginners. Learn Python fundamentals, then build three real projects: a Space Invaders arcade game, data visualizations with matplotlib, and a web app with Django. No prior programming experience required. Updated for Python 3.12.",
      "publisher": "No Starch Press",
      "format": "Paperback",
      "price": "39.95"
    }
  },
  {
    "id": "BK-011",
    "fields": {
      "title": "MLA Handbook",
      "author": "The Modern Language Association of America",
      "isbn": "978-1603293518",
      "edition": "9th Edition",
      "subject": "Reference > Writing > Citation",
      "description": "The authoritative guide to MLA style for research papers, theses, and dissertations. Covers formatting, citation, and documentation for print and digital sources. Includes guidance on avoiding plagiarism, inclusive language, and accessible design.",
      "publisher": "Modern Language Association",
      "format": "Paperback",
      "price": "16.00"
    }
  },
  {
    "id": "BK-012",
    "fields": {
      "title": "Human Anatomy & Physiology Laboratory Manual, Cat Version",
      "author": "Elaine N. Marieb, Lori A. Smith",
      "isbn": "978-0134806365",
      "edition": "13th Edition",
      "subject": "Science > Biology > Anatomy & Physiology",
      "description": "The #1 A&P lab manual with clear step-by-step dissection instructions, full-color photographs, and review sheets for every exercise. Cat version includes complete cat dissection guide. Features Mastering A&P access for virtual lab simulations and pre-lab quizzes.",
      "publisher": "Pearson",
      "format": "Spiral-bound",
      "price": "134.99"
    }
  }
]
```

## Expected Ranking Behavior

### Query: "calculus early transcendentals stewart"
1. BK-001 (exact title + author match) — score >0.95
2. All others — score <0.1

### Query: "intro to psychology"
1. BK-002 (introductory psychology — "Introduction" in subject) — score >0.7
2. All others — score <0.15

### Query: "data structures java"
1. BK-004 (Data Structures and Algorithms in Java) — score >0.8
2. BK-010 (Python Crash Course — programming but wrong language) — score 0.05-0.15
3. All others — score <0.05

### Query: "python programming beginners"
1. BK-010 (Python Crash Course — "beginners", "Python", "programming") — score >0.8
2. BK-004 (Data Structures in Java — programming but wrong language) — score 0.05-0.15

### Query: "ap biology study guide"
1. BK-005 (AP Biology study guide) — score >0.9
2. BK-012 (A&P lab manual — biology-adjacent) — score 0.1-0.2
