package sg.alitec.flutter_tsc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import com.example.tscdll.TscWifiActivity;

/** FlutterTscPlugin */
public class FlutterTscPlugin implements FlutterPlugin, MethodCallHandler {

  private TscWifiActivity TscEthernetDll = new TscWifiActivity();

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    final MethodChannel channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "flutter_tsc");
    channel.setMethodCallHandler(this);
  }

  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_tsc");
    channel.setMethodCallHandler(new FlutterTscPlugin());
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "inbound":
        handleInbound(call, result);
        break;
      case "outbound":
        handleOutbound(call, result);
        break;
      default:
        result.notImplemented();
    }
  }

  private void handleInbound(@NonNull MethodCall call, @NonNull Result result) {
    String ipAddress = call.argument("ipAddress");
    String lot = call.argument("lot");
    String product = call.argument("product");
    String number = call.argument("number");
    String purchaseUom = call.argument("purchaseUom");
    String uom = call.argument("uom");
    String uomQty = call.argument("uomQty");
    String printedName = call.argument("printedName");
    String done = call.argument("done");
    String poNum = call.argument("origin");
    String vendor = call.argument("contact");
    String expiryDate = call.argument("expiryDate");
    String currentDate = call.argument("currentDate");
    String location = call.argument("location");
    String staffId = call.argument("staffId");

    if (ipAddress == null || lot == null || product == null || number == null ||
        purchaseUom == null || uom == null || uomQty == null || printedName == null ||
        done == null || poNum == null || vendor == null || expiryDate == null ||
        currentDate == null || location == null || staffId == null) {
      result.error("INVALID_ARGUMENT", "One or more required arguments are missing", null);
      return;
    }

    int num = Integer.parseInt(number);
    String uomString = uom.equals(purchaseUom) ? done + " " + uom : uomQty + " " + printedName + " (" + done + " " + uom + ")";
    String expString = "EXP: " + expiryDate;

    TscEthernetDll.openport(ipAddress, 9100, 50);
    TscEthernetDll.setup(76, 25, 4, 12, 0, 3, 0);
    TscEthernetDll.clearbuffer();
    TscEthernetDll.sendcommand("DIRECTION 1\n");
    TscEthernetDll.sendcommand("SET COUNTER @1 1\n");
    if (num < 10) {
      TscEthernetDll.sendcommand("@1 = \"1\"\n");
    } else if (num < 100) {
      TscEthernetDll.sendcommand("@01 = \"1\"\n");
    } else {
      TscEthernetDll.sendcommand("@001 = \"1\"\n");
    }
    TscEthernetDll.barcode(16, 0, "128", 46, 2, 0, 2, 2, lot);
    TscEthernetDll.printerfont(328, 8, "3", 0, 1, 1, "Sea Bulk");
    TscEthernetDll.printerfont(552, 16, "3", 0, 1, 1, staffId);
    TscEthernetDll.printerfont(14, 80, "2", 0, 1, 1, product);
    TscEthernetDll.printerfont(328, 80, "2", 0, 1, 1, vendor);
    TscEthernetDll.printerfont(14, 104, "2", 0, 1, 1, uomString);
    TscEthernetDll.printerfont(328, 104, "2", 0, 1, 1, poNum);
    TscEthernetDll.printerfont(14, 128, "2", 0, 1, 1, expString);
    TscEthernetDll.printerfont(328, 128, "2", 0, 1, 1, currentDate);
    TscEthernetDll.sendcommand("TEXT 42,160,\"3\",0,1,1,@1\n");
    TscEthernetDll.printerfont(96, 160, "2", 0, 1, 1, "of");
    TscEthernetDll.printerfont(128, 160, "3", 0, 1, 1, number);
    TscEthernetDll.printerfont(228, 160, "4", 0, 1, 1, "INBOUND");
    TscEthernetDll.printerfont(462, 160, "3", 0, 1, 1, location);
    TscEthernetDll.printlabel(num, 1);
    TscEthernetDll.closeport();

    result.success("Success inbound printing");
  }

  private void handleOutbound(@NonNull MethodCall call, @NonNull Result result) {
    String ipAddress = call.argument("ipAddress");
    String label = call.argument("label");
    String product = call.argument("product");
    String qtyDone = call.argument("qtyDone");
    String expireDate = call.argument("expireDate");
    String packDate = call.argument("packDate");
    String invoice = call.argument("invoice");
    String po = call.argument("po");
    String customer = call.argument("customer");
    String outlet = call.argument("outlet");
    String number = call.argument("number");
    String ref = call.argument("ref");
    String internalNote = call.argument("internalNote");

    if (ipAddress == null || label == null || product == null || qtyDone == null ||
        expireDate == null || packDate == null || invoice == null || po == null ||
        customer == null || outlet == null || number == null || ref == null ||
        internalNote == null) {
      result.error("INVALID_ARGUMENT", "One or more required arguments are missing", null);
      return;
    }

    int num = Integer.parseInt(number);
    String expireDateString = expireDate.isEmpty() ? "" : "EXPIRY DATE: " + expireDate;

    TscEthernetDll.openport(ipAddress, 9100, 50);
    TscEthernetDll.setup(76, 25, 4, 12, 0, 3, 0);
    TscEthernetDll.clearbuffer();
    TscEthernetDll.sendcommand("DIRECTION 1\n");
    TscEthernetDll.sendcommand("SET COUNTER @1 1\n");
    TscEthernetDll.barcode(16, 0, "128", 32, 2, 0, 2, 2, label);
    TscEthernetDll.printerfont(328, 7, "3", 0, 1, 1, "Sea Bulk");

    if (!expireDateString.isEmpty()) {
      TscEthernetDll.printerfont(14, 60, "2", 0, 1, 1, expireDateString);
    }

    TscEthernetDll.printerfont(14, 88, "2", 0, 1, 1, product);
    TscEthernetDll.printerfont(14, 112, "2", 0, 1, 1, qtyDone);
    TscEthernetDll.printerfont(14, 144, "2", 0, 1, 1, "PACK DATE:");
    TscEthernetDll.printerfont(14, 164, "2", 0, 1, 1, packDate);
    TscEthernetDll.printerfont(352, 160, "3", 0, 1, 1, number);
    TscEthernetDll.sendcommand(String.format("TEXT 600,40,\"2\",0,1,1,3, \"%s\"\n", invoice).getBytes());
    TscEthernetDll.sendcommand(String.format("TEXT 600,60,\"2\",0,1,1,3, \"%s\"\n", po).getBytes());
    TscEthernetDll.sendcommand(String.format("TEXT 600,88,\"2\",0,1,1,3, \"%s\"\n", customer).getBytes());
    TscEthernetDll.sendcommand(String.format("TEXT 600,112,\"2\",0,1,1,3, \"%s\"\n", outlet).getBytes());
    TscEthernetDll.printerfont(416, 160, "3", 0, 1, 1, ref);
    TscEthernetDll.printerfont(560, 160, "4", 0, 1, 1, internalNote);
    TscEthernetDll.printlabel(num, 1);
    TscEthernetDll.closeport();

    result.success("Success outbound printing");
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    // Clean up when the plugin is detached from the engine
  }
}
