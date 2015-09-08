package de.idvos.fastonlineidentification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import de.idvos.fastonlineidentification.sdk.R;

public class InstructionBar {
	
	private static final String DIALOG_ID = "show_dialog_inst_id";
	private static final String GUIDE_SIZE = "guide_size";
	private static final String MESSAGES = "msg";
	
	public static final int INSTRUCTION_SHOW_FRONT = 1;
	public static final int INSTRUCTION_SHOW_BACK = 2;
	public static final int INSTRUCTION_SHOW_HOLOGRAPH = 3;
	public static final int INSTRUCTION_SHOW_FACE = 4;
	public static final int INSTRUCTION_READ_SERIAL_NUMBER = 5;
	
	public static final int INSTRUCTION_COUNT = 5;

	public static class Instruction {
		
		private int dialogId = 0;
		private int guideSize1 = 0;
		private int guideSize2 = 0;
        private double aspectRatio = 0;
		private String[] messages = null;

        public int getDialogId() {
            return dialogId;
        }

        public static Instruction parseInstruction(JSONObject jInstruction) throws JSONException {
			Instruction instruction = new Instruction();
			instruction.dialogId = jInstruction.getInt(DIALOG_ID);
			
			if (jInstruction.has(GUIDE_SIZE)) {
//				JSONArray jGuideSize = jInstruction.getJSONArray(GUIDE_SIZE);
                double aspectRatio = jInstruction.getDouble(GUIDE_SIZE);
                instruction.aspectRatio = aspectRatio;
//				instruction.guideSize1 = jGuideSize.getInt(0);
//				instruction.guideSize2 = jGuideSize.getInt(1);
			}
			
			JSONArray jMessages = jInstruction.getJSONArray(MESSAGES);
			instruction.messages = new String[jMessages.length()];
			for (int i=0; i<jMessages.length(); i++) {
				instruction.messages[i] = jMessages.getString(i);
			}
			
			return instruction;
		}
		
		private Instruction() {
			
		}
		
		public String getMessage() {
			if (messages.length==0) {
				return "";
			}
			else {
				return messages[0];
			}
		}
		
		public String getDetailedMessage() {
			if (messages.length>1) {
				return messages[1];
			}
			else {
				return null;
			}
		}
		
		public boolean hasMask() {
//            return aspectRatio > 0;
			return guideSize1!=0 && guideSize2!=0;
		}
		
		public double getMaskRatio() {
			float ratio = 0;
			
			if (guideSize2>guideSize1) {
				ratio = (float) guideSize1 / (float) guideSize2;
			}
			else {
				ratio = (float) guideSize2 / (float) guideSize1;
			}

			return ratio;
//			return aspectRatio;
		}
		
	}
	
	private View mInstructionBar;
	private TextView mInstructionCounter;
	private TextView mInstruction;
	private TextView mInstructionDetailed;
	private View mTanInput;
	
	private RelativeLayout mWindowMask;
	private View mWindowCam;
	
	public InstructionBar(Activity activity) {
		mInstructionBar = activity.findViewById(R.id.instructionbar);
		mInstructionCounter = (TextView) activity.findViewById(R.id.text_instruction_counter);
		mInstruction = (TextView) activity.findViewById(R.id.text_instruction);
		mInstructionDetailed = (TextView) activity.findViewById(R.id.text_instruction_detailed);
		mTanInput = activity.findViewById(R.id.tan);
		mWindowMask = (RelativeLayout)activity.findViewById(R.id.mask_window);
		mWindowCam = activity.findViewById(R.id.cam_window);
	}

    public void hideMask(){
        for (int i = 0; i < mWindowMask.getChildCount(); i++) {
            View currentChild = mWindowMask.getChildAt(i);
            if(currentChild.getId() != R.id.tan && currentChild.getId() != de.idvos.fastonlineidentification.sdk.R.id.cam_window)
                currentChild.setBackgroundColor(Color.TRANSPARENT);
        }

    }

    public void showMask(){
        for (int i = 0; i < mWindowMask.getChildCount(); i++) {
            View currentChild = mWindowMask.getChildAt(i);
            if(currentChild.getId() != R.id.tan && currentChild.getId() != R.id.cam_window)
                currentChild.setBackgroundResource(R.color.identification_cam_mask);
        }
    }
	
	public void showInstruction(Instruction instruction) {
		if (instruction!=null) {
			mInstructionBar.setVisibility(View.VISIBLE);
			
			if (instruction.dialogId>0 && instruction.dialogId<=INSTRUCTION_COUNT) {
				mInstructionCounter.setText(instruction.dialogId + "/" + INSTRUCTION_COUNT);
			}
			else {
				mInstructionCounter.setText(" ");
			}


			
			mInstruction.setText(instruction.getMessage());
			
			String detailedMessage = instruction.getDetailedMessage();
			if (detailedMessage!=null) {
				mInstructionDetailed.setVisibility(View.VISIBLE);
				mInstructionDetailed.setText(detailedMessage);
                mInstructionDetailed.setSelected(true);
			}
			else {
				mInstructionDetailed.setVisibility(View.GONE);
			}
			
//			if (true) {
			if (instruction.hasMask()) {
				mWindowMask.setVisibility(View.VISIBLE);
				
				double maskRatio = instruction.getMaskRatio();
				int maskWidth = mWindowMask.getWidth();
				
				int camWidth = maskWidth*4/5;
				int camHeight = (int) (maskRatio*camWidth);
				
				LayoutParams params = (LayoutParams) mWindowCam.getLayoutParams();
				params.width = camWidth;
				params.height = camHeight;
				mWindowCam.setLayoutParams(params);
			}
			else {
				mWindowMask.setVisibility(View.INVISIBLE);
			}
		}
		else {
			mInstructionBar.setVisibility(View.GONE);
			mWindowMask.setVisibility(View.INVISIBLE);
		}
	}
	
	public void showTanInstruction(Resources res) {
		Instruction instruction = new Instruction();
		instruction.messages = new String[] { res.getString(R.string.identification_wait_for_tan) };
		showInstruction(instruction);
	}
	
	public void showTanDialog(Resources res) {
		Instruction instruction = new Instruction();
		instruction.guideSize1 = mTanInput.getWidth();
		instruction.guideSize2 = mTanInput.getHeight();
		instruction.messages = new String[] { res.getString(R.string.identification_enter_tan) };
		showInstruction(instruction);
		mTanInput.setVisibility(View.VISIBLE);
	}
	
//	public void hideTanDialog() {
//		mTanInput.setVisibility(View.GONE);
//	}
	
}
