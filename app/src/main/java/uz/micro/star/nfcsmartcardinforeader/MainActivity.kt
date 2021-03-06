package uz.micro.star.nfcsmartcardinforeader

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.peerbits.creditCardNfcReader.CardNfcAsyncTask
import com.peerbits.creditCardNfcReader.utils.CardNfcUtils
import uz.micro.star.humo_atto_visa_union_pay_nfc_info_reader.nfc.CardNfcManager
import uz.micro.star.humo_atto_visa_union_pay_nfc_info_reader.nfc.SPEC
import uz.micro.star.humo_atto_visa_union_pay_nfc_info_reader.nfc.bean.Card
import uz.micro.star.nfcsmartcardinforeader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), CardNfcAsyncTask.CardNfcInterface {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mCardNfcManager: CardNfcManager
    private var mSafeExit = false

    private var mCardNfcAsyncTask: CardNfcAsyncTask? = null
    private var mNfcAdapter: NfcAdapter? = null
    private var mIntentFromCreate = false

    private var card: String? = null
    private var cardType: String? = null
    private var expiredDate: String? = null
    private var mCardNfcUtils: CardNfcUtils? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (mNfcAdapter == null) {
            /* TextView noNfc = findViewById(android.R.id.candidatesArea);
            noNfc.setVisibility(View.VISIBLE);*/
        } else {
            mCardNfcUtils = CardNfcUtils(this)
            mIntentFromCreate = true
//            onNewIntent(intent)
        }
        mCardNfcManager =
            CardNfcManager(
                this
            )
        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (!mCardNfcManager.readCard(intent) { event, objs ->
                if (event == SPEC.EVENT.IDLE) {
                    //            showProgressBar();
                } else if (event == SPEC.EVENT.FINISHED) {
                    //            hideProgressBar();
                    val card: Card? = if (objs.isNotEmpty()) objs[0] as Card? else null
                    buildResult(card)
                }
            }) {
            Toast.makeText(this, "Xatolik !!!!", Toast.LENGTH_SHORT).show()
        }
        if (mNfcAdapter != null && mNfcAdapter!!.isEnabled) {
            mCardNfcAsyncTask = CardNfcAsyncTask.Builder(this, intent, mIntentFromCreate)
                .build()
        }
    }

    private fun buildResult(card: Card?) {
        if (card != null && !card.hasReadingException()) {
            if (card.isUnknownCard) {
//    ret.putExtra(RET, NfcReaderApplication.getStringResource(R.string.info_nfc_unknown));
            } else {
                binding.nfcInfo.text =
                    card.serialAndDate.cardNumber + "\n" + card.serialAndDate.cardExpiredDate
                Toast.makeText(this, binding.nfcInfo.text.toString(), Toast.LENGTH_SHORT).show()
                Log.d("SERIAL", "buildResult: " + binding.nfcInfo.text.toString())
            }
        } else {
//            ret.putExtra(RET,NfcReaderApplication.getStringResource(R.string.info_nfc_error));
        }
    }

    override fun onBackPressed() {
        if (mSafeExit) {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        mCardNfcManager.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mCardNfcManager.onResume()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            if (mCardNfcManager.updateStatus()) {
                //loadDefaultPage
            }
            Handler().postDelayed({ mSafeExit = true }, 800)
        } else {
            mSafeExit = false
        }
    }

    override fun cardIsReadyToRead() {
        card = mCardNfcAsyncTask?.cardNumber
        card = getPrettyCardNumber(card!!)
        expiredDate = mCardNfcAsyncTask?.cardExpireDate
        cardType = mCardNfcAsyncTask?.cardType
//        val cardHolderFirstName: String = mCardNfcAsyncTask?.cardFirstName!!
//        val cardHolderLastName: String = mCardNfcAsyncTask?.cardFirstName!!
        val cardCvv: Int = mCardNfcAsyncTask?.cardCvv!!
        binding.nfcInfo.text =
            card + "\n" + expiredDate + "\n" + cardType


        /*   mPutCardContent.setVisibility(View.GONE);
        mCardNumberText.setText(card + cardType);
        mExpireDateText.setText(expiredDate);*/
        Toast.makeText(this, "Details: \n$card \n$cardType \n$expiredDate \n$cardCvv", Toast.LENGTH_SHORT).show()
        parseCardType(cardType!!)
    }

    private fun getPrettyCardNumber(card: String): String {
        val div = " - "
        return (card.substring(0, 4) + div + card.substring(4, 8) + div + card.substring(8, 12)
                + div + card.substring(12, 16))
    }

    private fun parseCardType(cardType: String) {
        when (cardType) {
            CardNfcAsyncTask.CARD_UNKNOWN -> {
                Toast.makeText(
                    this,
                    "unknown",
                    Toast.LENGTH_LONG
                ).show()
            }
            CardNfcAsyncTask.CARD_VISA -> {
                //   mCardLogoIcon.setImageResource(R.mipmap.visa_logo);
            }
            CardNfcAsyncTask.CARD_MASTER_CARD -> {
                //   mCardLogoIcon.setImageResource(R.mipmap.master_logo);
            }
            CardNfcAsyncTask.CARD_HUMO -> {
                Toast.makeText(this, "HUMO READING", Toast.LENGTH_SHORT).show()
            }
        }
    }
}