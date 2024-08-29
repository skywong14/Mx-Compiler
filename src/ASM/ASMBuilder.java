package ASM;

import ASM.operand.Memory;
import ASM.operand.PhysicalReg;
import ASM.operand.VirtualReg;
import ASM.section.ASMFunction;
import ASM.section.DataSection;
import ASM.section.RodataSection;
import ASM.section.TextSection;
import IR.IRBuilder;
import IR.IRStmts.*;

public class ASMBuilder {
    public IRBuilder irBuilder;
    public VirtualReg virtualReg;
    // public PhysicalReg physicalReg;
    public Memory memory;

    public TextSection textSection = new TextSection();
    public DataSection dataSection = new DataSection();
    public RodataSection rodataSection = new RodataSection();


    void init(IRBuilder irBuilder_) {
        irBuilder = irBuilder_;
        virtualReg = new VirtualReg();
        memory = new Memory();
    }
    public ASMBuilder(IRBuilder irBuilder_) {
        init(irBuilder_);
    }

    void buildStringConst() {
        for (IRStmt irStmt: irBuilder.constantStmts) {
            if (irStmt instanceof StringDeclareStmt stmt) {
                String asmString = StringDeclareStmt.convert2riscv(stmt.printValue);
                String label = stmt.dest.substring(1); // remove @
                rodataSection.addStringConst(label, asmString);
            }
        }
    }

    void buildGlobalVariables() {
        for (IRStmt irStmt: irBuilder.irStmts) {
            if (irStmt instanceof GlobalVariableDeclareStmt stmt) {
                String label = stmt.name.substring(1); // remove @
                dataSection.addGlobalVariable(label, "0");
            }
        }
    }

    int getSpSize(FunctionImplementStmt irFunction) {
        int spSize = 0;
        for (Block block : irFunction.blocks) {
            for (IRStmt irStmt : block.stmts) {
                spSize += irStmt.getSpSize();
            }
        }
        return spSize * 4;
    }

    void buildFunctions(FunctionImplementStmt irFunction) {
        ASMFunction asmFunction = new ASMFunction(irFunction.name, getSpSize(irFunction));
        // get sp size first


    }

    public void visitProgram() {
        buildStringConst();

        buildGlobalVariables();

        for (IRStmt irStmt: irBuilder.irStmts)
            if (irStmt instanceof FunctionImplementStmt irFunction) {
                buildFunctions(irFunction);
            }
    }
}
