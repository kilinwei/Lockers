package com.xyf.lockers.app;

import com.xyf.lockers.R;

/**
 * @项目名： Lockers
 * @包名： com.xyf.lockers.app
 * @文件名: Constants
 * @创建者: kilin
 * @创建时间: 2019/3/11 23:56
 * @描述： TODO
 */
public interface Constants {
    String SP_ALL_LOCKERS_STATUS = "sp_all_lockers_status";
    String SP_UP_DOWN_ANGLE = "sp_up_down_angle";
    String SP_LEFT_RIGHT_ANGLE = "sp_left_right_angle";
    String SP_ROTATE_ANGLE = "sp_rotate_angle";
    /**
     * 超过这个分数,认定是同一个人
     */
    int PASS_SCORE = 80;

    int[] OPEN_DOOR_AUDIOS = new int[]{
            R.raw.open_door_1,
            R.raw.open_door_2,
            R.raw.open_door_3,
            R.raw.open_door_4,
            R.raw.open_door_5,
            R.raw.open_door_6,
            R.raw.open_door_7,
            R.raw.open_door_8,
            R.raw.open_door_9,
            R.raw.open_door_10,
            R.raw.open_door_11,
            R.raw.open_door_12,
            R.raw.open_door_13,
            R.raw.open_door_14,
            R.raw.open_door_15,
            R.raw.open_door_16,
            R.raw.open_door_17,
            R.raw.open_door_18,
            R.raw.open_door_19,
            R.raw.open_door_20,
            R.raw.open_door_21,
            R.raw.open_door_22,
            R.raw.open_door_23,
            R.raw.open_door_24,
            R.raw.open_door_25,
            R.raw.open_door_26,
            R.raw.open_door_27,
            R.raw.open_door_28,
            R.raw.open_door_29,
            R.raw.open_door_30,
            R.raw.open_door_31,
            R.raw.open_door_32
    };
}
