mod bridge;
mod ui;
mod model;

use anyhow::Result;
use clap::Parser;

/// ReRanker UI — a terminal interface for testing the Java re-ranking library.
#[derive(Parser)]
#[command(name = "reranker-ui", version, about)]
struct Cli {
    /// Path to the Java library JAR file
    #[arg(short, long, default_value = "../reranker-core/build/libs/reranker-core-0.1.0-SNAPSHOT.jar")]
    jar_path: String,

    /// Run in non-interactive mode with a query
    #[arg(short, long)]
    query: Option<String>,

    /// Path to a JSON file containing documents to rank
    #[arg(short, long)]
    documents: Option<String>,
}

fn main() -> Result<()> {
    let cli = Cli::parse();

    match cli.query {
        Some(query) => {
            println!("Batch mode: ranking documents for query '{}'", query);
            println!("JAR path: {}", cli.jar_path);
            println!("Documents: {:?}", cli.documents);
            println!("\n[JNI bridge not yet implemented — see specs/features/005-rust-jni-bridge.md]");
        }
        None => {
            println!("Interactive TUI mode");
            println!("JAR path: {}", cli.jar_path);
            println!("\n[TUI not yet implemented — see specs/features/006-rust-tui.md]");
        }
    }

    Ok(())
}
