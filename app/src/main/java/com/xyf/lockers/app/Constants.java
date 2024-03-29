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
    String SP_PASSWORD_JSON = "sp_password_json";
    /**
     * 通行策略的时候,超过这个分数,认定是同一个人
     */
    float PASS_SCORE = 80f;
    /**
     * 通行策略的时候,超过这个分数,认定是同一个人
     */
    float REGISTER_SCORE = 10f;
    int STORAGE = 1;
    int TEMPORARY_TAKE = 2;
    int TAKE = 3;

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

    String[] names = new String[]{
            "open_door_1.mp3",
            "open_door_2.mp3",
            "open_door_3.mp3",
            "open_door_4.mp3",
            "open_door_5.mp3",
            "open_door_6.mp3",
            "open_door_7.mp3",
            "open_door_8.mp3",
            "open_door_9.mp3",
            "open_door_10.mp3",
            "open_door_11.mp3",
            "open_door_12.mp3",
            "open_door_13.mp3",
            "open_door_14.mp3",
            "open_door_15.mp3",
            "open_door_16.mp3",
            "open_door_17.mp3",
            "open_door_18.mp3",
            "open_door_19.mp3",
            "open_door_20.mp3",
            "open_door_21.mp3",
            "open_door_22.mp3",
            "open_door_23.mp3",
            "open_door_24.mp3",
            "open_door_25.mp3",
            "open_door_26.mp3",
            "open_door_27.mp3",
            "open_door_28.mp3",
            "open_door_29.mp3",
            "open_door_30.mp3",
            "open_door_31.mp3",
            "open_door_32.mp3"

    };
}
