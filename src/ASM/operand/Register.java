package ASM.operand;

public class Register{
    public String name;
    public int value;
    public int id;
    public Register(String name, int id){
        this.name = name;
        this.id = id;
        this.value = 0;
    }
    public void set(int value){ this.value = value; }
    public int get(){ return this.value; }
    public String toString(){ return this.name; }
}