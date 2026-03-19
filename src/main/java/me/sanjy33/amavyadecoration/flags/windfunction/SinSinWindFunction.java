package me.sanjy33.amavyadecoration.flags.windfunction;

public class SinSinWindFunction implements WindFunction {

    private static SinSinWindFunction instance = null;
    public static SinSinWindFunction getInstance() {
        if (instance == null) {
            instance = new SinSinWindFunction();
        }
        return instance;
    }

    private SinSinWindFunction() {

    }

    @Override
    public float getValue(float x) {
        return (float) (Math.sin(x/2d) * Math.sin(x) * 0.75f);
    }
}
