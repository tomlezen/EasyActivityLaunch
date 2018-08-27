package com.tlz.easyactivitylaunch.example

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.tlz.easyactivitylaunch.*

@EasyLaunch(parameters = [MainActivity.TestClass::class, String::class])
@EasyLaunchForResult(parameters = [Int::class, Array<MainActivity.TestClass>::class])
@EasyLaunch1(parameters = [Array<String>::class, Float::class])
@EasyLaunch2(parameters = [CharSequence::class, Double::class])
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = Intent()
        launchMainActivity(TestClass(), "1")
//        intent.putExtra()
    }

    class TestClass() : Parcelable{
        constructor(parcel: Parcel) : this() {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {

        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<TestClass> {
            override fun createFromParcel(parcel: Parcel): TestClass {
                return TestClass(parcel)
            }

            override fun newArray(size: Int): Array<TestClass?> {
                return arrayOfNulls(size)
            }
        }

    }
}
