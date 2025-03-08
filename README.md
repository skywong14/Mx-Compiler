# Mx-Compiler

### Development Timeline
- From **July 24, 2024** to **October 30, 2024**.
- Optimization enhancements: **February 23, 2025 - March 8, 2025**

---

## Compiler Overview

This project implements a compiler for the **Mx** language with the following key features:

### 1. **Register Allocation**
- Uses a **linear scan** strategy for efficient register allocation.

### 2. **Advanced Optimizations**
- **Tail Recursion Optimization**: Converts tail-recursive functions into iterative loops to improve performance and reduce stack usage.
- **Global-to-Local Transformation**: Optimizes global variables by replacing them with local equivalents where applicable.
- **Dead Code Elimination (DCE)**: Identifies and removes code that does not affect program output, enhancing overall efficiency.
- **Function Inlining**:  Replaces calls to small, frequently-used functions with the function's body directly at the call site. This reduces call overhead and enables further optimizations. (Added)
- **Common Subexpression Elimination (CSE)**: Identifies and eliminates redundant calculations of the same expression within a given scope. (Added)
- **Constant Folding**: Evaluates constant expressions at compile time rather than runtime, simplifying expressions and reducing execution overhead. (Added)
- **Constant Propagation (SCCP)**: Replaces variables that have known constant values with those constants, further enabling constant folding and other optimizations. (Added)