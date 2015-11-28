package Support;

public class Coor1D implements Coordinate {

    public double[] switchcoor;
    public double hostcoor;

    public Coor1D(double [] coor, double h) {
        this.switchcoor = coor;
        this.hostcoor = h;
    }
    public Coor1D(double [] coor) {
        this(coor,0.0);
    }
    public Coor1D(int [] intcoor) {
        double [] coor = new double[intcoor.length];
        for (int i = 0 ; i < intcoor.length; i++)
            coor[i] = intcoor[i]*1.0/Coordinate.MAX_coordinate_int;
        this.switchcoor = coor;
        this.hostcoor = 0.0;
    }

    public Coor1D(Coor1D CC, double hcoor) {
        this.switchcoor = ( CC).switchcoor;
        this.hostcoor = hcoor;
    }
    public double dist_to_switch(Coordinate x, int dim) {
        Coor1D xx = (Coor1D) x;
        double min = max_coordinate;
        for (int i = 0 ; i < dim; i++) {
            double p = circulardist(xx.switchcoor[i],this.switchcoor[i]);
            if (p < min) min = p;
        }
        return min;
    }

    @Override
    public void generate_from_content(long x) {
        // TODO Auto-generated method stub

    }
    public static  double circulardist(double a, double b) {
        double p = Math.abs(a-b);
        if (max_coordinate-p < p) return max_coordinate-p;
        else return p;
    }

    @Override
    public double dist_to_switch(Coordinate x) {
        return this.dist_to_switch(x,this.switchcoor.length);
    }

    @Override
    public double dist_to_Host(double host) {
        return circulardist(host,hostcoor);
    }
    public double dist_to_Host(Coordinate hostC) {
        return circulardist(((Coor1D)hostC).hostcoor,hostcoor);
    }
    public String toString() {
        String a=new String();
        for (int i = 0 ; i < switchcoor.length; i++)
            a+= switchcoor[i]+" ";
        a+=","+hostcoor;
        return a;
    }
    @Override
    public double dist_to_switch_fixdim(Coordinate x, int dim) {
        dim = dim % this.switchcoor.length;
        Coor1D xx = (Coor1D) x;

        return circulardist(xx.switchcoor[dim],this.switchcoor[dim]);
    }
    @Override
    public int getdim() {
        return switchcoor.length;
    }

}
