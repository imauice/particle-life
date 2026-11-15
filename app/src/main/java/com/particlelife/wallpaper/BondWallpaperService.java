package com.particlelife.wallpaper;

import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.view.MotionEvent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Random;

/** Educational 2D CHNOPS particle model, not molecular dynamics or real chemical reactions. */
public class BondWallpaperService extends WallpaperService {
    // C, H, O, N, P, S: approximate atomic masses and common simplified valences.
    private static final int[] COLOR = {0xffa8b4c8,0xfff1f5f9,0xfffb7185,0xff60a5fa,0xfff59e0b,0xfffacc15};
    private static final int[] VALENCE = {4,1,2,3,3,2};
    private static final float[] MASS = {12.01f,1.008f,16.00f,14.01f,30.97f,32.06f};
    private static final float[] RADIUS = {3.0f,2.0f,3.15f,3.05f,3.6f,3.5f};
    private static final int[] WEIGHT = {28,38,17,12,3,2};
    private final Random random = new Random();
    @Override public Engine onCreateEngine() { return new BioEngine(); }

    private class Atom {
        float x,y,vx,vy; int element,degree;
        Atom(float x,float y,int element) {
            this.x=x; this.y=y; this.element=element;
            vx=(random.nextFloat()-.5f)*.65f;
            vy=(random.nextFloat()-.5f)*.65f;
        }
    }
    private static class Bond { int a,b; Bond(int a,int b){this.a=a;this.b=b;} }

    private class BioEngine extends Engine {
        private final Handler handler=new Handler(Looper.getMainLooper());
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final ArrayList<Atom> atoms=new ArrayList<>();
        private final ArrayList<Bond> bonds=new ArrayList<>();
        private final SharedPreferences prefs=getSharedPreferences("settings",MODE_PRIVATE);
        private boolean visible=false;
        private int width=400,height=800;
        private float scale=1f;
        private int ticks=0;
        private final Runnable frame=new Runnable(){@Override public void run(){
            if(!visible)return;
            long start=android.os.SystemClock.uptimeMillis();
            simulate();draw();
            int fps=Math.max(10,Math.min(60,prefs.getInt("fps",30)));
            handler.postDelayed(this,Math.max(1,1000/fps-(android.os.SystemClock.uptimeMillis()-start)));
        }};
        BioEngine(){setTouchEventsEnabled(true);}
        private float clamp(float x,float a,float b){return Math.max(a,Math.min(b,x));}
        private int randomElement(){int v=random.nextInt(100),sum=0;for(int i=0;i<WEIGHT.length;i++){sum+=WEIGHT[i];if(v<sum)return i;}return 0;}
        private void add(float x,float y,int count){
            for(int i=0;i<count&&atoms.size()<650;i++){
                float a=random.nextFloat()*6.2831853f;
                float r=(float)Math.sqrt(random.nextFloat())*35f;
                atoms.add(new Atom(clamp(x+(float)Math.cos(a)*r,5,width-5),clamp(y+(float)Math.sin(a)*r,5,height-5),randomElement()));
            }
        }
        private void reset(){
            atoms.clear();bonds.clear();ticks=0;
            int n=Math.max(100,Math.min(600,prefs.getInt("particles",420)));
            int cols=3,rows=5,groups=cols*rows;
            for(int k=0;k<groups;k++){
                float x=width*((k%cols)+.5f)/cols;
                float y=height*((k/cols)+.5f)/rows;
                int start=atoms.size();
                add(x,y,n/groups+(k<n%groups?1:0));
                float a=random.nextFloat()*6.2831853f;
                for(int i=start;i<atoms.size();i++){
                    atoms.get(i).vx+=(float)Math.cos(a)*.35f;
                    atoms.get(i).vy+=(float)Math.sin(a)*.35f;
                }
            }
        }
        @Override public void onSurfaceChanged(SurfaceHolder holder,int format,int w,int h){
            super.onSurfaceChanged(holder,format,w,h);
            scale=Math.max(1f,Math.min(3f,w/400f));
            width=Math.max(20,Math.round(w/scale));height=Math.max(20,Math.round(h/scale));reset();
        }
        @Override public void onVisibilityChanged(boolean v){visible=v;handler.removeCallbacks(frame);if(v)handler.post(frame);}
        @Override public void onSurfaceDestroyed(SurfaceHolder holder){visible=false;handler.removeCallbacks(frame);super.onSurfaceDestroyed(holder);}
        @Override public void onDestroy(){visible=false;handler.removeCallbacks(frame);super.onDestroy();}
        @Override public void onTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN)add(e.getX()/scale,e.getY()/scale,18);}
        private boolean connected(int i,int j){for(Bond b:bonds)if(b.a==i&&b.b==j)return true;return false;}
        private void simulate(){
            ticks++;
            int n=atoms.size();float[] fx=new float[n],fy=new float[n];
            float noise=prefs.getInt("noise",25)/100f;
            float chance=prefs.getInt("bond",60)/100f;
            float strength=prefs.getInt("strength",55)/100f;
            for(int i=0;i<n;i++){
                fx[i]=(random.nextFloat()-.5f)*noise*.9f;
                fy[i]=(random.nextFloat()-.5f)*noise*.9f;
            }
            // Spring-like bonds with occasional stochastic breakage. Bond energies are not simulated.
            for(int k=bonds.size()-1;k>=0;k--){
                Bond bond=bonds.get(k);Atom a=atoms.get(bond.a),b=atoms.get(bond.b);
                float dx=b.x-a.x,dy=b.y-a.y;
                float d=(float)Math.sqrt(dx*dx+dy*dy);
                boolean stretched=d>43f;
                boolean thermalBreak=random.nextFloat()<(0.00004f+noise*.00015f)*(1.3f-strength*.6f);
                if(stretched||thermalBreak){a.degree--;b.degree--;bonds.remove(k);continue;}
                d=Math.max(.01f,d);
                float target=RADIUS[a.element]+RADIUS[b.element]+5f;
                float f=(d-target)*(.008f+strength*.035f);
                fx[bond.a]+=f*dx/d;fy[bond.a]+=f*dy/d;
                fx[bond.b]-=f*dx/d;fy[bond.b]-=f*dy/d;
            }
            for(int i=0;i<n;i++){
                Atom a=atoms.get(i);
                for(int j=i+1;j<n;j++){
                    Atom b=atoms.get(j);
                    float dx=b.x-a.x,dy=b.y-a.y,d2=dx*dx+dy*dy;
                    if(d2>.0f&&d2<1156f){
                        float d=(float)Math.sqrt(d2),nx=dx/d,ny=dy/d;
                        float minDist=RADIUS[a.element]+RADIUS[b.element]+2f;
                        if(d<minDist){float rep=(minDist-d)*.095f;
                            fx[i]-=rep*nx;fy[i]-=rep*ny;fx[j]+=rep*nx;fy[j]+=rep*ny;
                        }
                        // Weak encounter attraction makes groups interact without claiming real atomic forces.
                        if(d>minDist&&d<26f){float f=.004f;
                            fx[i]+=f*nx;fy[i]+=f*ny;fx[j]-=f*nx;fy[j]-=f*ny;
                        }
                        if(d<17f&&a.degree<VALENCE[a.element]&&b.degree<VALENCE[b.element]
                                &&bonds.size()<1100&&random.nextFloat()<chance*.007f&&!connected(i,j)){
                            bonds.add(new Bond(i,j));a.degree++;b.degree++;
                        }
                    }
                }
            }
            for(int i=0;i<n;i++){
                Atom p=atoms.get(i);
                // Inertia varies with approximate atomic mass, compressed for visual stability.
                float inertia=(float)Math.sqrt(MASS[p.element]/12.01f);
                inertia=clamp(inertia,.65f,1.65f);
                p.vx=(p.vx+fx[i]/inertia)*.975f;
                p.vy=(p.vy+fy[i]/inertia)*.975f;
                float speed=(float)Math.sqrt(p.vx*p.vx+p.vy*p.vy);
                if(speed>2.4f){p.vx*=2.4f/speed;p.vy*=2.4f/speed;}
                p.x+=p.vx;p.y+=p.vy;
                if(p.x<5||p.x>width-5){p.x=clamp(p.x,5,width-5);p.vx*=-.85f;}
                if(p.y<5||p.y>height-5){p.y=clamp(p.y,5,height-5);p.vy*=-.85f;}
            }
        }
        private void draw(){
            Canvas canvas=null;
            try{
                canvas=getSurfaceHolder().lockCanvas();if(canvas==null)return;
                canvas.drawColor(0xff050b16);canvas.save();canvas.scale(scale,scale);
                paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1.35f);
                paint.setColor(0xb0a6c7e6);
                for(Bond b:bonds){Atom a=atoms.get(b.a),c=atoms.get(b.b);canvas.drawLine(a.x,a.y,c.x,c.y,paint);}
                paint.setStyle(Paint.Style.FILL);
                for(Atom a:atoms){paint.setColor(COLOR[a.element]);canvas.drawCircle(a.x,a.y,RADIUS[a.element],paint);}
                canvas.restore();
            }catch(Exception ignored){}finally{
                if(canvas!=null)try{getSurfaceHolder().unlockCanvasAndPost(canvas);}catch(Exception ignored){}
            }
        }
    }
}
