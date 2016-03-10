package com.example.user.oxgame;

import android.app.Service;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;


public class Draw extends View implements Runnable {
    private final static byte EMPTY = 0;
    private final static byte CIRCLE = 1;
    private final static byte CROSS = 2;
    private final byte[][] victory = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};//the condition of victory
    private int round = 0;
    private int[][] board = new int[9][2];
    private Handler mHandler = new Handler();
    private Paint p;
    private float H, W; //螢幕高寬
    private float gridPointA_x, gridPointB_y, gridLength; //取井字左上方兩點的X、Y ， 取井字長度的三分之一當一單位
    private Context mContext;

    public Draw(Context context) {
        super(context);
        mContext = context;
        Log.d("Test", "constructor");
        p = new Paint();
        p.setAntiAlias(true);
        p.setStrokeWidth(10);
        p.setStyle(Paint.Style.STROKE);
        setGrid();
        newGame();
    }

    //draw
    @Override
    public void onDraw(Canvas canvas) {
        Log.d("Test", "onDraw");
        p.setColor(Color.BLUE);
        super.onDraw(canvas);

        //draw table
        p.setStrokeWidth(10);
        canvas.drawLine(gridPointA_x - 5, gridPointB_y - gridLength, gridPointA_x - 5, gridPointB_y + gridLength * 2, p);
        canvas.drawLine(gridPointA_x + gridLength - 5, gridPointB_y - gridLength, gridPointA_x + gridLength - 5, gridPointB_y + gridLength * 2, p);
        canvas.drawLine(gridPointA_x - gridLength, gridPointB_y, gridPointA_x + gridLength * 2, gridPointB_y, p);
        canvas.drawLine(gridPointA_x - gridLength, gridPointB_y + gridLength, gridPointA_x + gridLength * 2, gridPointB_y + gridLength, p);

        //draw O or X
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                if(board[3*i+j][0] == CIRCLE){
                    Log.d("onDraw", "CIRCLE");
                    p.setColor(Color.RED);
                    p.setStrokeWidth(18);
                    canvas.drawOval(gridPointA_x - gridLength + 20 + gridLength*i,
                            gridPointB_y - gridLength + 20 + gridLength*j,
                            gridPointA_x - 20 + gridLength*i,
                            gridPointB_y - 20 + gridLength*j,
                            p);//左上右下
                }
                else if(board[3*i+j][0] == CROSS){
                    Log.d("onDraw", "CROSS");
                    p.setColor(Color.GREEN);
                    p.setStrokeWidth(18);
                    canvas.drawLine(gridPointA_x - gridLength + 20 + gridLength * i,
                            gridPointB_y - gridLength + 20 + gridLength * j,
                            gridPointA_x - 20 + gridLength * i,
                            gridPointB_y - 20 + gridLength * j,
                            p);//start x,y end x,y
                    canvas.drawLine(gridPointA_x - 20 + gridLength*i,
                            gridPointB_y - gridLength + 20 + gridLength*j,
                            gridPointA_x - gridLength + 20 + gridLength*i,
                            gridPointB_y - 20 + gridLength*j,
                            p);//start x,y end x,y
                }
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d("Test", "sizeChange");
    }

    @Override
    public void run() {
        Log.d("Test", "run");
        for (int i = 0; i < 1000; i++) {
            mHandler.post(new Runnable() {
                public void run() {
                    invalidate();
                }
            });
        }
    }

    //set relative place
    private void setGrid() { //Android 螢幕長的為高，短的為寬，但在畫畫時的座標都是左上為原點
        Log.d("Test", "setGrid");
        H = getResources().getDisplayMetrics().heightPixels;
        W = getResources().getDisplayMetrics().widthPixels;
        if (H > W) {
            gridPointA_x = W / 3;
            gridPointB_y = (H / 2 - W / 6);
            gridLength = W / 3;
        } else {
            W = getResources().getDisplayMetrics().heightPixels;
            H = getResources().getDisplayMetrics().widthPixels;
            gridPointA_x = (H / 2 - W / 6);
            gridPointB_y = W / 3;
            gridLength = W / 3;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.d("Test", "Touch");
            if (whoWin() > 0) {
                newGame();
                Log.d("Touch", "whoWin");
                return false;
            }
            int row, col;
            if (H > W) {
                row = (int) (event.getX() / gridLength);
                col = (int) ((event.getY() - ((H - W) / 2)) / gridLength);
            } else {
                row = (int) (event.getX() - ((W - H) / 2) / gridLength);
                col = (int) (event.getY() / gridLength);
            }
            Log.d("Touch", "row" + row + "col" + col);
            if (row > 2 || col > 2) return true;

            if (board[row * 3 + col][0] == EMPTY) {
                Log.d("Touch", "setOX");
                Vibrator myVibrator = (Vibrator) mContext.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
                myVibrator.vibrate(30);
                round++;
                if (round > 5) {
                    for (int i = 0; i < 9; i++) {
                        if (board[i][1] < round - 5) {
                            board[i][0] = 0;
                            board[i][1] = 0;
                            new Thread().start();
                        }
                    }
                    Log.d("Touch", "round>5?");
                }
                Log.d("Touch","isEmpty");
                board[row * 3 + col][0] = playing();
                board[row * 3 + col][1] = round;
                new Thread(this).start();
                try {
                    if(whoWin() == CIRCLE){
                        Toast toastO = Toast.makeText(mContext, "O is the winner!!!", Toast.LENGTH_SHORT);
                        toastO.show();
                    }else if(whoWin() == CROSS){
                        Toast toastX = Toast.makeText(mContext, "X is the winner!!!", Toast.LENGTH_SHORT);
                        toastX.show();
                    }
                    if (whoWin() > 0) {
                        Toast.makeText(mContext, "Touch again to restart.", Toast.LENGTH_SHORT).show();
                        myVibrator.vibrate(500);
                    }
                } catch (Exception ex) {}
            }
            Log.d("Touch", "震動");
            //震動提示
        }
        return true;
    }

    //determine who is playing
    public byte playing(){
        if(round%2 == 1){
            return CIRCLE;
        }else{
            return CROSS;
        }
    }

    //determine the winner
    public byte whoWin() {
        for(int i = 0; i < victory.length; i++){
            for(int j = 0;j < 3 && board[victory[i][j]][0] == playing(); j++){
                if(j == 2){
                    if(playing() == CROSS){
                        return CROSS;
                    }
                    else{
                        return CIRCLE;
                    }
                }
            }
        }
        return 0;
    }

    //restart a game
    public void newGame(){
        round = 0;
        //clear the board
        for(int i = 0; i<3; i++){
            for(int j = 0; j<3; j++){
                board[3*i+j][0] = 0;
            }
        }
        new Thread(this).start();
    }
}
