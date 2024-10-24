.PHONY: build
build:
	find src -name '*.java' | xargs javac -d bin -cp /ulib/antlr-4.13.2-complete.jar
.PHONY: run
run:
	java -cp /ulib/antlr-4.13.2-complete.jar:bin Main -emit-llvm

.PhONY: asm_output
asm_output:
	java -cp /ulib/antlr-4.13.2-complete.jar:bin Main < test.mx > test.s -emit-llvm

.PHONY: ir_output
ir_output:
	java -cp /ulib/antlr-4.13.2-complete.jar:bin Main < test.mx > test.ll -emit-llvm

.PHONY: ir_single
ir_single:
	testcases/codegen/scripts/test_llvm_ir.bash 'java -cp /ulib/antlr-4.13.2-complete.jar:bin Main -emit-llvm' testcases/codegen/t22.mx src/IR/builtin/builtin.ll

.PHONY: ir_all
ir_all:
	testcases/codegen/scripts/test_llvm_ir_all.bash 'java -cp /ulib/antlr-4.13.2-complete.jar:bin Main -emit-llvm' testcases/codegen src/IR/builtin/builtin.ll

.PHONY: asm_single
asm_single:
	testcases/codegen/scripts/test_asm.bash 'java -cp /ulib/antlr-4.13.2-complete.jar:bin Main -S' testcases/codegen/t22.mx src/IR/builtin/builtin.s

.PHONY: asm_all
asm_all:
	testcases/codegen/scripts/test_asm_all.bash 'java -cp /ulib/antlr-4.13.2-complete.jar:bin Main -S' testcases/codegen  src/IR/builtin/builtin.s

.PHONY: init
init:
	export PATH="/usr/local/opt/bin:$PATH"

.PHONY: clang
clang:
	clang-18 -m32 output.ll src/IR/builtin/builtin.ll -o test

.PHONY: debug
debug:
	reimu --debug -i=test.s -o=output.s

.PHONY: oj_test # same condition as Online Judge
oj_test:
	testcases/codegen/scripts/test_asm_all.bash 'java -cp /ulib/antlr-4.13.2-complete.jar:bin Main -S' testcases/codegen  src/IR/builtin/empty.s