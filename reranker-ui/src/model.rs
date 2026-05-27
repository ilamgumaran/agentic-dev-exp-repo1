#![allow(dead_code)]

use serde::{Deserialize, Serialize};
use std::collections::HashMap;

/// A document to be ranked, mirroring the Java Document record.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Document {
    pub id: String,
    pub fields: HashMap<String, String>,
}

/// A ranked result returned from the Java library.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct RankedResult {
    pub document_id: String,
    pub score: f64,
    pub component_scores: Vec<ComponentScore>,
}

/// A single scorer's contribution to the final score.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ComponentScore {
    pub scorer_name: String,
    pub value: f64,
}

/// Configuration for a scoring pipeline.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PipelineConfig {
    pub scorers: Vec<ScorerConfig>,
    pub aggregator: AggregatorConfig,
    pub top_k: Option<usize>,
}

/// Configuration for a single scorer.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ScorerConfig {
    pub name: String,
    pub weight: f64,
    pub params: HashMap<String, f64>,
}

/// Configuration for the score aggregator.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AggregatorConfig {
    pub strategy: String,
    pub weights: Vec<f64>,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_document_serialization() {
        let doc = Document {
            id: "doc1".to_string(),
            fields: HashMap::from([
                ("title".to_string(), "Blue Shoes".to_string()),
            ]),
        };
        let json = serde_json::to_string(&doc).unwrap();
        let deserialized: Document = serde_json::from_str(&json).unwrap();
        assert_eq!(deserialized.id, "doc1");
        assert_eq!(deserialized.fields.get("title").unwrap(), "Blue Shoes");
    }

    #[test]
    fn test_ranked_result_serialization() {
        let result = RankedResult {
            document_id: "doc1".to_string(),
            score: 0.95,
            component_scores: vec![
                ComponentScore {
                    scorer_name: "bm25".to_string(),
                    value: 0.8,
                },
            ],
        };
        let json = serde_json::to_string(&result).unwrap();
        let deserialized: RankedResult = serde_json::from_str(&json).unwrap();
        assert_eq!(deserialized.document_id, "doc1");
        assert!((deserialized.score - 0.95).abs() < 0.001);
    }
}
