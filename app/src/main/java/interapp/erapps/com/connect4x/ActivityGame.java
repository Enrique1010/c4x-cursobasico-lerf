package interapp.erapps.com.connect4x;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Image;
import android.media.SoundPool;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityGame extends AppCompatActivity implements View.OnClickListener{

    private Handler mHandler = new Handler();
    private SoundPool mSoundPool;

    private RelativeLayout mBoardGrid;
    private LinearLayout mL1,mL2,mL3,mL4,mL5,mL6,mL7;
    private EditText player1,other;
    private int moveDelay = 700;
    private boolean mPlayerCanMove = true;
    private int mTADASound;
    private int mSADSound;
    private int mAPPLAUSESound;
    private Board mBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        setSupportActionBar((Toolbar) findViewById(R.id.mainToolbar));

        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mSoundPool = new SoundPool.Builder().build();
        mTADASound = mSoundPool.load(this, R.raw.cheering, 1);
        mSADSound = mSoundPool.load(this, R.raw.sad_trombone, 1);
        mAPPLAUSESound = mSoundPool.load(this, R.raw.fake_applause, 1);

        mBoardGrid = findViewById(R.id.mainA_RL_FR_RL_grid);
        checkIfValidDisksNumber();

        mL1 = findViewById(R.id.mainA_RL_FR_RL_LL_col1);
        mL2 = findViewById(R.id.mainA_RL_FR_RL_LL_col2);
        mL3 = findViewById(R.id.mainA_RL_FR_RL_LL_col3);
        mL4 = findViewById(R.id.mainA_RL_FR_RL_LL_col4);
        mL5 = findViewById(R.id.mainA_RL_FR_RL_LL_col5);
        mL6 = findViewById(R.id.mainA_RL_FR_RL_LL_col6);
        mL7 = findViewById(R.id.mainA_RL_FR_RL_LL_col7);


        mL1.setOnClickListener(this);
        mL2.setOnClickListener(this);
        mL3.setOnClickListener(this);
        mL4.setOnClickListener(this);
        mL5.setOnClickListener(this);
        mL6.setOnClickListener(this);
        mL7.setOnClickListener(this);

        player1 = findViewById(R.id.mainA_RL_TV_playerName);
        other = findViewById(R.id.mainA_RL_TV_AIName);
        mBoard = new Board(6,7, 4);
        clearBoard();

    }

    private void checkIfValidDisksNumber() {
        // Double check if each row has exactly the same number of disk positions
        int nephews = ((ViewGroup) mBoardGrid.getChildAt(0)).getChildCount();
        for (int x = mBoardGrid.getChildCount() - 1; x >= 0; x--) {
            ViewGroup currLine = (ViewGroup) mBoardGrid.getChildAt(x);
            if (nephews != currLine.getChildCount()) {
                throw new RuntimeException("All lines must have same number of disks.");
            }
        }
    }


    /**
     * Set all disks images to white image - transparent and reset all board counters for the disks.
     */
    private void clearBoard() {
        for (int x = mBoardGrid.getChildCount() - 1; x >= 0; x--) {
            ViewGroup currLine = (ViewGroup) mBoardGrid.getChildAt(x);
            for (int y = currLine.getChildCount() - 1; y >= 0; y--) {
                ((ImageView) currLine.getChildAt(y)).setImageResource(R.drawable.white);
            }
        }

        mPlayerCanMove = true;

        // Reset all board counters
        mBoard.clearBoard();
    }


    /**
     * Will animate a disk drawable image as falling to the indicated position.
     * @param player on behalf on whom the move is made - will designate the disk drawable.
     * @param row position where to insert the new disk.
     * @param col position where to insert the new disk.
     */
    public void dropDisk(Board.PLAYERS player, int row, int col) {
        ViewGroup currLine = (ViewGroup) mBoardGrid.getChildAt(col);

        ImageView diskPosition = (ImageView) currLine.getChildAt(row);

        diskPosition.setTranslationY(-1000);

        int diskDrawable = (player == Board.PLAYERS.PLAYER ? R.drawable.yellow
                : R.drawable.red);
        diskPosition.setImageResource(diskDrawable);

        diskPosition.animate().translationYBy(1000).rotation(-480).setDuration(500);
    }


    /**creating the menu and options**/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;    // signal that we've dealt with the menu and want it displayed.
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem disksNeededToWin = menu.findItem(R.id.mainM_I_modifyDisksToWin);
        disksNeededToWin.setTitle(
                String.format("Disks Needed To Win: %d", mBoard.getDisksNeededForWin()));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.mainM_I_newGame) {
            clearBoard();
        }

        else if (id == R.id.mainM_I_resetScores) {
            mBoard.resetScores();
            updateScores();
        }

        else if (id == R.id.mainM_I_modifyDisksToWin) {
            showWinningDisksNumberPickerDialog();
        }

        else if (id == R.id.mainM_I_instantAIMove) {
            if (menuItem.isChecked()) {
                moveDelay = 700;
                menuItem.setChecked(false);
            }
            else {
                moveDelay = 0;
                menuItem.setChecked(true);
            }
        }
        else if (id == R.id.mainM_I_changeNames) {
            enableNamesToBeEdited();
        }

        else if (id == R.id.mainM_I_settings) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "No available settings atm.", Snackbar.LENGTH_SHORT);
            snackbar.show();

        }
        else if (id == R.id.help) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "!Just one Player Game! " +
                            " |Click on Column for lets Start", 4000);
            snackbar.show();

        }

        return true;    // we've dealt with this, no need for other listeners to check it
    }

    /**
     * Enable the two Edit Texts for the user to edit the names of players.
     * <br>Auto hide soft keyboard and re-disable the fields afterwards.
     */
    private void enableNamesToBeEdited() {
        player1.setFocusableInTouchMode(true);
        other.setFocusableInTouchMode(true);

        player1.requestFocus();
        final InputMethodManager imm
                = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        // To allow starting the query when user presses "Enter" on the softkey keyboard.
        other.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_DONE)
                        || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                        && (event.getAction() == KeyEvent.ACTION_DOWN))) {

                    // To hide the soft keyboard when user presses "Enter"
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    player1.setFocusableInTouchMode(false);
                    other.setFocusableInTouchMode(false);
                    other.clearFocus();
                }
                return true;
            }
        });
    }


    /**
     * Present the user with a cool {@link android.widget.NumberPicker} {@link Dialog} for him to
     * choose the number of disks required to be in a continuous line for winning the game,
     * between limits got from the {@link Board}.
     */
    private void showWinningDisksNumberPickerDialog() {
        final Dialog d = new Dialog(ActivityGame.this);
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.number_picker_dialog);
        View v = d.getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);
        Button b1 = d.findViewById(R.id.numberPicker_BTN_set);
        Button b2 = d.findViewById(R.id.numberPicker_BTN_cancel);
        final NumberPicker np = d.findViewById(R.id.numberPicker);
        np.setMaxValue(mBoard.getMaxDisksToWin());
        np.setMinValue(mBoard.getMinDisksToWin());
        np.setValue(mBoard.getDisksNeededForWin());
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(new android.widget.NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal) {
                np.playSoundEffect(SoundEffectConstants.CLICK);
            }
        });
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBoard.modifyNoOfDisksToWin(np.getValue());
                d.dismiss();
                clearBoard();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }



    /**
     * Try to insert a new disk on the indicated column on the board.
     * <br> If that column is already full nothing happens.
     * If player's move succeeded, will also try to continue with a move on behalf of the AI.
     * @param column position where to insert the new disk on behalf of the player.
     */
    private void playerMoveToColumn(int column) {
        Integer row = mBoard.makePlayerMove(column);
        if (row != null) {  // valid move (invalid if the column would be full).
            dropDisk(Board.PLAYERS.PLAYER, row, column);
            if (mBoard.getWinner() != null || mBoard.isDraw()) {
                updateScores();
                showGameOverDialog();
            }
            else {
                mPlayerCanMove = false;
                makeAIMove();
            }
        }
    }

    /**
     * Make a ~random move on behalf of the other player - the AI.
     * <br>Will auto check before and after for if the game is already won or a draw.
     */
    private void makeAIMove() {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Integer[] AIMovePosition = mBoard.makeAIMove();
                if (AIMovePosition != null) {
                    dropDisk(Board.PLAYERS.AI, AIMovePosition[0], AIMovePosition[1]);
                    if (mBoard.getWinner() != null || mBoard.isDraw()) {
                        updateScores();
                        showGameOverDialog();
                    }
                    mPlayerCanMove = true;
                }
            }
        }, moveDelay);
    }

    /**select options when player 1 clicking*/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mainA_RL_FR_RL_LL_col1:
                if (mPlayerCanMove) {
                    playerMoveToColumn(0);
                }
                break;
            case R.id.mainA_RL_FR_RL_LL_col2:
                if (mPlayerCanMove) {
                    playerMoveToColumn(1);
                }
                break;
            case R.id.mainA_RL_FR_RL_LL_col3:
                if (mPlayerCanMove) {
                    playerMoveToColumn(2);
                }
                break;
            case R.id.mainA_RL_FR_RL_LL_col4:
                if (mPlayerCanMove) {
                    playerMoveToColumn(3);
                }
                break;
            case R.id.mainA_RL_FR_RL_LL_col5:
                if (mPlayerCanMove) {
                    playerMoveToColumn(4);
                }
                break;
            case R.id.mainA_RL_FR_RL_LL_col6:
                if (mPlayerCanMove) {
                    playerMoveToColumn(5);
                }
                break;
            case R.id.mainA_RL_FR_RL_LL_col7:
                if (mPlayerCanMove) {
                    playerMoveToColumn(6);
                }
                break;
            default :
                Log.d(getClass().getSimpleName(), "Invalid View clicked !?");
        }
    }

    /**
     * Updates the two scores placeholders with values read from the app's {@link Board}.
     */
    private void updateScores() {
        TextView playerScoreTV = findViewById(R.id.mainA_RL_TV_playerScore);
        TextView AIScoreTV = findViewById(R.id.mainA_RL_TV_AIScore);

        playerScoreTV.setText(Integer.toString(mBoard.getScores()[0]));
        AIScoreTV.setText(Integer.toString(mBoard.getScores()[1]));
    }


    /**
     * Show an Alert Dialog informing about the winner or that the game is a draw, letting the
     * user choose whether he wants to play another game or no.
     * <br>Will also play a ending game sound depending of the winners.
     */
    private void showGameOverDialog() {
        boolean gameOver = false;
        String winner = "";
        int soundToPlay = 0;

        if (mBoard.getWinner() != null) {
            winner = mBoard.getWinner().equals(Board.PLAYERS.PLAYER.toString()) ?
                    player1.getText().toString() : other.getText().toString();
            soundToPlay = mBoard.getWinner().equals(Board.PLAYERS.PLAYER.toString()) ?
                    mTADASound : mSADSound;
            winner += " won!";
            gameOver = true;
        }
        else if (mBoard.isDraw()) {
            soundToPlay = mAPPLAUSESound;
            winner = "It's a draw!";
            gameOver = true;
        }

        if (gameOver) {
            mSoundPool.play(soundToPlay, 1, 1, 0, 0, 1);
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(this, R.style.AppTheme_AlertDialogStyle)
                            .setTitle(winner)
                            .setMessage("Wanna test your luck again?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    clearBoard();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(ActivityGame.this, "BBye!",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
            builder.show();
        }
    }

    /**for finish the sound*/
    @Override
    protected void onDestroy() {
        mSoundPool.release();
        super.onDestroy();
    }
}