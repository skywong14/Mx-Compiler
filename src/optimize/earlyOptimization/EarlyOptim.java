package optimize.earlyOptimization;

import optimize.IRCode;

public class EarlyOptim {
    public void optimize(IRCode irCode) {
        // Constant Folding
        new ConstantFolding().optimize(irCode);

        // Constant Propagation
//        new ConstantPropagation().optimize(irCode);
    }
}
