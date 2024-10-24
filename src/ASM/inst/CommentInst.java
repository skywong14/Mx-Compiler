package ASM.inst;

public class CommentInst extends ASMInst {
    public String comment;

    public CommentInst(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        if (!comment.isEmpty())
            return "    # " + comment;
        else
            return "";
    }
}
