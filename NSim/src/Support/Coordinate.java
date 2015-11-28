package Support;

public interface Coordinate {
    static final double max_coordinate= 1.0;
    static final int MAX_coordinate_int= 1000000;
    static final int D1 = 1;
    static final int D2 = 2;
    public double dist_to_switch(Coordinate x);
    public double dist_to_switch(Coordinate x,int dim);
    public double dist_to_switch_fixdim(Coordinate x, int dim);

    public double dist_to_Host(double host);
    public double dist_to_Host(Coordinate hostC);
    public int getdim();
    public void generate_from_content(long x);
    public String toString();
}
