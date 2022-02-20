package com.example.musicplayer

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.databinding.ActivityFeedbackBinding
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class Feedback : AppCompatActivity() {
    private lateinit var binding: ActivityFeedbackBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.darkBlueThemeNav)
        //Initializing Binding
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "FEEDBACK"

        binding.sendFD.setOnClickListener {
            val feedbackMsg =
                binding.feedbackFD.text.toString() + "\n From \n " + binding.emailFD.text.toString()
            val subject = binding.topicFD.text.toString()
            val user = "zahraamiri1381216@gmail.com"
            val password = "kagurasougo"
            val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (feedbackMsg.isNotEmpty() && subject.isNotEmpty() && cm.activeNetworkInfo?.isConnectedOrConnecting == true) {
                Thread {
                    try {
                        val properties = Properties()
                        properties["mail.smtp.auth"] = "true"
                        properties["mail.smtp.starttls.enable"] = "true"
                        properties["mail.smtp.host"] = "smtp.gmail.com"
                        properties["mail.smtp.port"] = "587"
                        val session = Session.getInstance(properties, object : Authenticator() {
                            override fun getPasswordAuthentication(): PasswordAuthentication {
                                return PasswordAuthentication(user, password)
                            }
                        })
                        val email = MimeMessage(session)
                        email.subject = subject
                        email.setText(feedbackMsg)
                        email.setFrom(InternetAddress(user))
                        email.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user))
                        Transport.send(email)
                        finish()
                    } catch (e: Exception) {
                    }
                }.start()
            } else {
                Toast.makeText(this, "Something Went Wrong :(", Toast.LENGTH_SHORT).show()
            }
        }
    }
}