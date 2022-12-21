public class FactorCounter {

    private int sum_count;
    private int mul_count;

    public FactorCounter() {
        this.sum_count = 0;
        this.mul_count = 0;
    }

    public void sumAdd(int add) {
        this.sum_count += add;
    }

    public void mulAdd(int mul) {
        this.mul_count += mul;
    }

    public int getSumCount() {
        return sum_count;
    }

    public int getMulCount() {
        return mul_count;
    }

    @Override
    public String toString() {
        return this.sum_count + "," + this.mul_count;
    }
}
