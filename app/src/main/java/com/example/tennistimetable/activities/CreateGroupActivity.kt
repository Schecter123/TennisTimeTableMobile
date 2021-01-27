package com.example.tennistimetable.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.example.tennistimetable.R
import com.example.tennistimetable.firebase.FirestoreClass
import com.example.tennistimetable.models.Group
import kotlinx.android.synthetic.main.activity_create_group.*
import kotlinx.android.synthetic.main.activity_my_profile.*

class CreateGroupActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupActionBar()

        rb_sparing.setOnClickListener {
            iv_board_image.setBackgroundResource(R.drawable.s2_icon)
        }
        rb_training.setOnClickListener {
            iv_board_image.setBackgroundResource(R.drawable.t_icon)
        }

        btn_create_group.setOnClickListener {
            showProgressDialog(resources.getString(R.string.please_wait))
            createGroup()
        }
    }

    private fun createGroup() {

        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())

        var groupType = 11

        if (rb_sparing.isChecked) {
            groupType = 0
        } else if (rb_training.isChecked) {
            groupType = 1
        }

        var group = Group(
            et_group_name.text.toString(),
            groupType,
            assignedUsersArrayList
        )

        FirestoreClass().createGroup(this, group)


    }

    fun groupCreatedSuccessfully() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_create_board_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_create_board_activity.setNavigationOnClickListener { onBackPressed() }
    }
}