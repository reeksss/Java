public class Main {
    public static void main(String[] args) {
        double c = 0.01;
        double a = mySin(c);
        double b = Math.sin(c);
        double d = b-a;
        System.out.printf("Библиотечная - "+ b);
        System.out.println();
        System.out.printf("Функция - "+ a);
        System.out.println();
        System.out.printf("Разница - "+ d);
    }
    public static double mySin(double x) {
        return x;
    }
}
