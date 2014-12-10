package mendel.vptree;

/**
 * An interface that defines a single point in a vp-vptree. All points must have a
 * distance function that satisfies metric space.
 *
 * @author ctolooee
 */
public interface VPPoint extends Comparable<VPPoint> {

    public double getDistanceTo(VPPoint otherPoint);

}