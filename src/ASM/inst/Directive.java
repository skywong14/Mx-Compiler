package ASM.inst;

public class Directive extends ASMInst {
    public String directive;
    public String comment;

    public Directive(String directive, String comment) {
        this.directive = directive;
        this.comment = comment;
    }

    @Override
    public String toString() {
        return directive + (comment == null ? "" : " " + comment);
    }
}
