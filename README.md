# Mx-Compiler

### Development Timeline
From **July 24, 2018** to **October 30, 2024**.

---

## Compiler Overview

This project implements a compiler for the **Mx** language with the following key features:

### 1. **Register Allocation**
- Uses a **linear scan** strategy for efficient register allocation.

### 2. **Advanced Optimizations**
- **Tail Recursion Optimization**: Converts tail-recursive functions into iterative loops to improve performance and reduce stack usage.
- **Global-to-Local Transformation**: Optimizes global variables by replacing them with local equivalents where applicable.
- **Dead Code Elimination (DCE)**: Identifies and removes code that does not affect program output, enhancing overall efficiency.
