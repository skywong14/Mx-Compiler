.PHONY: build
build:
	find src -name '*.java' | xargs javac -d bin -cp /ulib/antlr-4.13.2-complete.jar
.PHONY: run
run:
	java -cp /ulib/antlr-4.13.2-complete.jar:bin Main -emit-llvm

.PHONY: outputFile
outputFile:
	java -cp /ulib/antlr-4.13.2-complete.jar:bin Main < test.mx > output.ll -emit-llvm

.PHONY: single_ir
single_ir:
	testcases/codegen/scripts/test_llvm_ir.bash 'java -cp /ulib/antlr-4.13.2-complete.jar:bin Main -emit-llvm' testcases/codegen/shortest_path/spfa.mx src/IR/builtin/builtin.ll

.PHONY: all_ir
all_ir:
	testcases/codegen/scripts/test_llvm_ir_all.bash 'java -cp /ulib/antlr-4.13.2-complete.jar:bin Main -emit-llvm' testcases/codegen src/IR/builtin/builtin.ll

.PHONY: init
init:
	export PATH="/usr/local/opt/bin:$PATH"

.PHONY: clang
clang:
	clang-18 -m32 output.ll src/IR/builtin/builtin.ll -o test
