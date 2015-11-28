package Support;

public class SmallWorldCoor implements Coordinate {
    public int width;
    public int height;
    public int depth;
    private int x,y,z;
    public int gx() {
        return x;
    }
    public int gy() {
        return y;
    }
    public int gz() {
        return z;
    }
    public int dim;
    public SmallWorldCoor(int a, int b, int c, int w, int h , int d) {
        width = w;
        height = h;
        depth = d;
        x= a;
        y = b*3;
        z = c;
        if (a % 2 != b %2) y ++;
        dim = 3;
    }
    public SmallWorldCoor(int a, int b,  int w, int h ) {
        width = w;
        height = h;
        x= a;
        y = b;
        dim = 2;
    }

    public SmallWorldCoor(int a, int w) {
        width = w;
        x= a;
        dim = 1;
    }

    private double dist_to_loc(int _x, int _y , int _z) {
        int dx = Math.abs(x-_x);
        int dy = Math.abs(y-_y);
        int dz = Math.abs(z-_z);
        if (dx > width - dx) dx = width - dx;
        if (dy > height * 3 - dy) dy = height * 3 - dy;
        if (dz > depth - dz) dz = depth - dz;
        return dx*dx*3 + dy*dy + dz*dz*2;
    }
    private double dist_to_loc(int _x, int _y) {
        int dx = Math.abs(x-_x);
        int dy = Math.abs(y-_y);
        if (dx > width - dx) dx = width - dx;
        if (dy > height - dy) dy = height - dy;
        return dx*dx+dy*dy;
    }
    private double dist_to_loc(int _x) {
        int dx = Math.abs(x-_x);
        if (dx > width - dx) dx = width - dx;
        return dx;
    }


    public double dist_to_switch(Coordinate tx) {
        SmallWorldCoor x = (SmallWorldCoor) tx;
        if (dim == 3) {
            double a  = dist_to_loc(x.gx(),x.gy(),x.gz());
            return a;
        }
        if (dim==2) {
            double a = dist_to_loc(x.gx(),x.gy());
            return a;
        }
        double a = dist_to_loc(x.gx());
        return a;
    }

    @Override
    public double dist_to_switch(Coordinate x, int dim) {
        System.out.println("do not use this dist_to_switch with given dim");
        return 0;
    }

    @Override
    public double dist_to_switch_fixdim(Coordinate x, int dim) {
        System.out.println("do not use this dist_to_switch_fixdim");
        return 0;
    }

    @Override
    public double dist_to_Host(double host) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double dist_to_Host(Coordinate hostC) {
        return dist_to_switch(hostC);
    }

    @Override
    public int getdim() {
        return dim;
    }

    @Override
    public void generate_from_content(long x) {
        System.out.println("do not use this generate_from_content");
    }
    public String toString() {
        if (dim==3)
            return x + " " + y + " " + z;
        if (dim==2)
            return x + " " + y ;
        return Integer.toString(x) ;
    }

}
