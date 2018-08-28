package com.tlz.easyactivitylaunch.example

import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import com.tlz.easyactivitylaunch.*

@EasyLaunch(parameters = [MainActivity.TestClass::class, String::class])
@EasyLaunchForResult(
        parameters = [Int::class, Array<MainActivity.TestClass>::class],
        fragmentSupport = true
)
@EasyLaunch1(
        nickName = "CustomFuncName",
        parameters = [Intent::class, Bundle::class],
        parameterNames = ["customParam1", "customParam2"],
        fragmentSupport = true
)
@EasyLaunch2(
        parameters = [IntArray::class, Double::class],
        fragmentSupport = true
)
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        launchMainActivity(TestClass(), "1")
    }

    class TestClass() : Parcelable {
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
