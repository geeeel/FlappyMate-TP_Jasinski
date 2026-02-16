package com.badlogic.Flappy.net;

public final class StateParser {

    private StateParser() {}

    // STATE;tick=10;P1=y,vy,alive,score;P2=y,vy,alive,score;T=x,gap|x,gap|...;
    public static StateSnapshot parse(String raw) {
        try {
            StateSnapshot s = new StateSnapshot();

            String[] parts = raw.split(";");
            for (String part : parts) {
                if (part.startsWith("STATE")) continue;

                if (part.startsWith("tick=")) {
                    s.tick = Integer.parseInt(part.substring("tick=".length()));
                } else if (part.startsWith("P1=")) {
                    String data = part.substring(3);
                    String[] v = data.split(",");
                    s.p1y = Float.parseFloat(v[0]);
                    s.p1vy = Float.parseFloat(v[1]);
                    s.p1alive = Integer.parseInt(v[2]);
                    s.p1score = Integer.parseInt(v[3]);
                } else if (part.startsWith("P2=")) {
                    String data = part.substring(3);
                    String[] v = data.split(",");
                    s.p2y = Float.parseFloat(v[0]);
                    s.p2vy = Float.parseFloat(v[1]);
                    s.p2alive = Integer.parseInt(v[2]);
                    s.p2score = Integer.parseInt(v[3]);
                } else if (part.startsWith("T=")) {
                    String tdata = part.substring(2);
                    if (tdata.isEmpty()) {
                        s.termoX = new float[0];
                        s.termoGap = new float[0];
                    } else {
                        String[] termos = tdata.split("\\|");
                        s.termoX = new float[termos.length];
                        s.termoGap = new float[termos.length];

                        for (int i = 0; i < termos.length; i++) {
                            String[] pair = termos[i].split(",");
                            s.termoX[i] = Float.parseFloat(pair[0]);
                            s.termoGap[i] = Float.parseFloat(pair[1]);
                        }
                    }
                }
            }

            return s;
        } catch (Exception e) {
            return null;
        }
    }
}
