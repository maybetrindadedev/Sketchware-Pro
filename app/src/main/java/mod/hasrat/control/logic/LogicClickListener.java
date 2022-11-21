package mod.hasrat.control.logic;

import static android.text.TextUtils.isEmpty;
import static com.besome.sketch.SketchApplication.getContext;
import static mod.SketchwareUtil.dpToPx;
import static mod.SketchwareUtil.getDip;

import android.text.InputType;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.besome.sketch.beans.ProjectFileBean;
import com.besome.sketch.editor.LogicEditorActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.sketchware.remod.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import a.a.a.ZB;
import a.a.a.aB;
import a.a.a.bB;
import a.a.a.eC;
import a.a.a.jC;
import a.a.a.uq;
import a.a.a.wB;
import mod.SketchwareUtil;
import mod.hasrat.dialog.SketchDialog;
import mod.hasrat.menu.ExtraMenuBean;
import mod.hey.studios.util.Helper;

public class LogicClickListener implements View.OnClickListener {

    private final eC projectDataManager;
    private final LogicEditorActivity logicEditor;
    private final ProjectFileBean projectFile;
    private final String eventName;
    private final String javaName;

    public LogicClickListener(LogicEditorActivity logicEditor) {
        this.logicEditor = logicEditor;
        projectDataManager = jC.a(logicEditor.B);
        projectFile = logicEditor.M;
        eventName = logicEditor.C + "_" + logicEditor.D;
        javaName = logicEditor.M.getJavaName();
    }

    private ArrayList<String> getUsedVariable(int type) {
        return projectDataManager.e(projectFile.getJavaName(), type);
    }

    private ArrayList<String> getUsedList(int type) {
        return projectDataManager.d(projectFile.getJavaName(), type);
    }

    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag();
        if (!isEmpty(tag)) {
            switch (tag) {
                case "listAddCustom":
                    addCustomList();
                    break;

                case "variableAddNew":
                    addCustomVariable();
                    break;

                case "variableRemove":
                    removeVariable();
                    break;

                case "listRemove":
                    removeList();
                    break;
            }
        }
    }

    private void addCustomVariable() {
        aB dialog = new aB(logicEditor);
        dialog.a(R.drawable.abc_96_color);
        dialog.b("Add a new custom variable");

        LinearLayout root = new LinearLayout(logicEditor);
        root.setOrientation(LinearLayout.VERTICAL);

        TextInputLayout modifierLayout = commonTextInputLayout();
        EditText modifier = commonEditText("private, public or public static (optional)");
        modifierLayout.addView(modifier);
        modifierLayout.setHelperText("Enter modifier e.g. private, public, public static, or empty (package private).");
        modifierLayout.setPadding(0, 0, 0, (int) getDip(8));
        root.addView(modifierLayout);

        TextInputLayout typeLayout = commonTextInputLayout();
        EditText type = commonEditText("Type, e.g. File");
        typeLayout.addView(type);
        root.addView(typeLayout);

        TextInputLayout nameLayout = commonTextInputLayout();
        EditText name = commonEditText("Name, e.g. file");
        nameLayout.addView(name);
        root.addView(nameLayout);

        TextInputLayout initializerLayout = commonTextInputLayout();
        EditText initializer = commonEditText("Initializer, e.g. new File() (optional)");
        initializerLayout.addView(initializer);
        root.addView(initializerLayout);

        ZB validator = new ZB(getContext(), nameLayout, uq.b, uq.a(), projectDataManager.a(projectFile));

        dialog.a(root);
        dialog.b(Helper.getResString(R.string.common_word_add), view -> {
            String variableModifier = modifier.getText().toString();
            variableModifier = isEmpty(variableModifier) ? "" : variableModifier + " ";
            String variableType = type.getText().toString();
            String variableName = name.getText().toString();
            String variableInitializer = initializer.getText().toString();

            boolean validType = !isEmpty(variableType);
            boolean validName = !isEmpty(variableName);
            boolean getsInitialized = !isEmpty(variableInitializer);

            if (validType) {
                typeLayout.setError(null);
            } else {
                if (validName) typeLayout.requestFocus();
                typeLayout.setError("Type can't be empty");
            }

            CharSequence nameError = nameLayout.getError();
            if (nameError == null || "Name can't be empty".contentEquals(nameError)) {
                if (validName) {
                    nameLayout.setError(null);
                } else {
                    nameLayout.requestFocus();
                    nameLayout.setError("Name can't be empty");
                }
            }

            if (validName && validType && validator.b()) {
                String toAdd = variableModifier + variableType + " " + variableName;
                if (getsInitialized) {
                    toAdd += " = " + variableInitializer;
                }
                logicEditor.b(6, toAdd);
                dialog.dismiss();
            }
        });
        dialog.a(Helper.getResString(R.string.common_word_cancel), Helper.getDialogDismissListener(dialog));
        dialog.show();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        modifierLayout.requestFocus();
    }

    private void removeVariable() {
        SketchDialog dialog = new SketchDialog(logicEditor);
        dialog.setTitle(Helper.getResString(R.string.logic_editor_title_remove_variable));
        dialog.setIcon(R.drawable.delete_96);

        RecyclerView recyclerView = new RecyclerView(logicEditor);
        recyclerView.setLayoutManager(new LinearLayoutManager(null));

        List<Item> data = new LinkedList<>();
        RemoveAdapter adapter = new RemoveAdapter(data);
        recyclerView.setAdapter(adapter);

        List<Pair<List<Integer>, String>> variableTypes = List.of(
                new Pair<>(List.of(ExtraMenuBean.VARIABLE_TYPE_BOOLEAN), "Boolean (%d)"),
                new Pair<>(List.of(ExtraMenuBean.VARIABLE_TYPE_NUMBER), "Number (%d)"),
                new Pair<>(List.of(ExtraMenuBean.VARIABLE_TYPE_STRING), "String (%d)"),
                new Pair<>(List.of(ExtraMenuBean.VARIABLE_TYPE_MAP), "Map (%d)"),
                new Pair<>(List.of(5, 6), "Custom Variable (%d)")
        );

        for (Pair<List<Integer>, String> variableType : variableTypes) {
            List<String> variableTypeInstances = new LinkedList<>();

            List<Integer> first = variableType.first;
            for (int i = 0; i < first.size(); i++) {
                Integer type = first.get(i);

                if (i == 0) {
                    variableTypeInstances = getUsedVariable(type);
                } else {
                    variableTypeInstances.addAll(getUsedVariable(type));
                }
            }

            for (int i = 0, size = variableTypeInstances.size(); i < size; i++) {
                String instanceName = variableTypeInstances.get(i);

                if (i == 0) data.add(new Item(String.format(variableType.second, size)));
                data.add(new Item(instanceName, R.string.logic_editor_message_currently_used_variable));
            }
        }

        dialog.setView(recyclerView);
        dialog.setPositiveButton(Helper.getResString(R.string.common_word_remove), v -> {
            for (Item item : data) {
                if (item.type == Item.TYPE_ITEM && item.isChecked) {
                    logicEditor.m(item.text);
                }
            }
            dialog.dismiss();
        });
        dialog.setNegativeButton(Helper.getResString(R.string.common_word_cancel), null);
        dialog.show();
    }

    private void addCustomList() {
        aB dialog = new aB(logicEditor);
        dialog.a(R.drawable.add_96_blue);
        dialog.b("Add a new custom List");

        LinearLayout root = new LinearLayout(logicEditor);
        root.setOrientation(LinearLayout.VERTICAL);

        TextInputLayout typeLayout = commonTextInputLayout();
        EditText type = commonEditText("Type, e.g. ArrayList<Data>");
        typeLayout.addView(type);

        TextInputLayout nameLayout = commonTextInputLayout();
        EditText name = commonEditText("Name, e.g. dataList");
        nameLayout.addView(name);

        root.addView(typeLayout);
        root.addView(nameLayout);

        ZB validator = new ZB(getContext(), nameLayout, uq.b, uq.a(), projectDataManager.a(projectFile));

        dialog.a(root);
        dialog.b(Helper.getResString(R.string.common_word_add), view -> {
            String variableType = type.getText().toString();
            String variableName = name.getText().toString();

            boolean validType = !isEmpty(variableType);
            boolean validName = !isEmpty(variableName);

            if (validType) {
                typeLayout.setError(null);
            } else {
                if (validName) typeLayout.requestFocus();
                typeLayout.setError("Type can't be empty");
            }

            CharSequence nameError = nameLayout.getError();
            if (nameError == null || "Name can't be empty".contentEquals(nameError)) {
                if (validName) {
                    nameLayout.setError(null);
                } else {
                    nameLayout.requestFocus();
                    nameLayout.setError("Name can't be empty");
                }
            }

            if (validType && validName && validator.b()) {
                logicEditor.a(4, variableType + " " + variableName + " = new ArrayList<>()");
                dialog.dismiss();
            }
        });
        dialog.a(Helper.getResString(R.string.common_word_cancel), Helper.getDialogDismissListener(dialog));
        dialog.show();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        typeLayout.requestFocus();
    }

    private void removeList() {
        aB dialog = new aB(logicEditor);
        dialog.b(Helper.getResString(R.string.logic_editor_title_remove_list));
        dialog.a(R.drawable.delete_96);
        View var2 = wB.a(logicEditor, R.layout.property_popup_selector_single);
        ViewGroup viewGroup = var2.findViewById(R.id.rg_content);

        ArrayList<String> listNumbers = getUsedList(ExtraMenuBean.LIST_TYPE_NUMBER);
        for (int i = 0, listIntSize = listNumbers.size(); i < listIntSize; i++) {
            if (i == 0) viewGroup.addView(commonTextView("List Integer (" + listIntSize + ")"));
            viewGroup.addView(getRemoveListCheckBox(listNumbers.get(i)));
        }

        ArrayList<String> listStrs = getUsedList(ExtraMenuBean.LIST_TYPE_STRING);
        for (int i = 0, listStrSize = listStrs.size(); i < listStrSize; i++) {
            if (i == 0) viewGroup.addView(commonTextView("List String (" + listStrSize + ")"));
            viewGroup.addView(getRemoveListCheckBox(listStrs.get(i)));
        }

        ArrayList<String> listMaps = getUsedList(ExtraMenuBean.LIST_TYPE_MAP);
        for (int i = 0, listMapSize = listMaps.size(); i < listMapSize; i++) {
            if (i == 0) viewGroup.addView(commonTextView("List Map (" + listMapSize + ")"));
            viewGroup.addView(getRemoveListCheckBox(listMaps.get(i)));
        }

        ArrayList<String> listCustom = getUsedList(4);
        for (int i = 0, listCustomSize = listCustom.size(); i < listCustomSize; i++) {
            if (i == 0) viewGroup.addView(commonTextView("List Custom (" + listCustomSize + ")"));
            viewGroup.addView(getRemoveListCheckBox(listCustom.get(i)));
        }

        dialog.a(var2);
        dialog.b(Helper.getResString(R.string.common_word_remove), view -> {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                if (viewGroup.getChildAt(i) instanceof CheckBox) {
                    CheckBox list = (CheckBox) viewGroup.getChildAt(i);
                    String listName = list.getText().toString();

                    if (list.isChecked()) {
                        // Since an in-use List can't be checked, just remove it
                        logicEditor.l(listName);
                    }
                }
            }
            dialog.dismiss();
        });
        dialog.a(Helper.getResString(R.string.common_word_cancel), Helper.getDialogDismissListener(dialog));
        dialog.show();
    }

    private TextInputLayout commonTextInputLayout() {
        TextInputLayout textInputLayout = new TextInputLayout(logicEditor);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(
                (int) getDip(8),
                0,
                (int) getDip(8),
                0
        );
        textInputLayout.setLayoutParams(layoutParams);
        return textInputLayout;
    }

    private TextView commonTextView(String text) {
        TextView textView = new TextView(logicEditor);
        textView.setText(text);
        textView.setPadding(
                (int) getDip(2),
                (int) getDip(4),
                (int) getDip(4),
                (int) getDip(4)
        );
        textView.setTextSize(14f);
        return textView;
    }

    private EditText commonEditText(String hint) {
        EditText editText = new EditText(logicEditor);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        editText.setPadding(
                (int) getDip(4),
                (int) getDip(8),
                (int) getDip(8),
                (int) getDip(8)
        );
        editText.setTextSize(16f);
        editText.setTextColor(0xff000000);
        editText.setHint(hint);
        editText.setHintTextColor(0xff607d8b);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setPrivateImeOptions("defaultInputmode=english;");
        return editText;
    }

    private CheckBox getRemoveListCheckBox(String listName) {
        return commonRemoveCheckBox(
                logicEditor.o.b(listName) || projectDataManager.b(javaName, listName, eventName),
                listName,
                R.string.logic_editor_message_currently_used_list);
    }

    private CheckBox commonRemoveCheckBox(boolean hasUses, String name, int toastMessageId) {
        CheckBox checkBox = new CheckBox(logicEditor);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) getDip(40),
                1);
        checkBox.setLayoutParams(params);
        checkBox.setText(name);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isChecked()) {
                if (hasUses) {
                    SketchwareUtil.toastError(Helper.getResString(toastMessageId), bB.TOAST_WARNING);
                    buttonView.setChecked(false);
                }
            }
        });
        return checkBox;
    }

    private class RemoveAdapter extends RecyclerView.a<RecyclerView.v> {

        private final List<Item> data;

        private RemoveAdapter(List<Item> data) {
            this.data = data;
        }

        @Override
        // RecyclerView.Adapter#getItemCount()
        public int a() {
            return data.size();
        }

        @Override
        // RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)
        public RecyclerView.v b(ViewGroup parent, int viewType) {
            if (viewType == Item.TYPE_TITLE) {
                TextView textView = new TextView(logicEditor);
                textView.setLayoutParams(new LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT));
                textView.setPadding(
                        dpToPx(2),
                        dpToPx(4),
                        dpToPx(4),
                        dpToPx(4)
                );
                textView.setTextSize(14);
                return new TitleHolder(textView);
            } else if (viewType == Item.TYPE_ITEM) {
                CheckBox checkBox = new CheckBox(logicEditor);
                checkBox.setLayoutParams(new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
                return new CheckBoxHolder(checkBox);
            } else {
                throw new IllegalStateException("Unknown view type " + viewType);
            }
        }

        @Override
        // RecyclerView.Adapter#onBindViewHolder(VH, int)
        public void b(RecyclerView.v holder, int position) {
            Item item = data.get(position);
            // RecyclerView.ViewHolder#getItemViewType()
            int viewType = holder.i();

            if (viewType == Item.TYPE_TITLE) {
                TitleHolder titleHolder = (TitleHolder) holder;
                titleHolder.title.setText(item.text);
            } else if (viewType == Item.TYPE_ITEM) {
                CheckBoxHolder checkBoxHolder = (CheckBoxHolder) holder;
                checkBoxHolder.checkBox.setText(item.text);
                checkBoxHolder.checkBox.setChecked(item.isChecked);

                checkBoxHolder.checkBox.setOnClickListener(v -> {
                    boolean isChecked = checkBoxHolder.checkBox.isChecked();
                    item.isChecked = isChecked;
                    if (item.type == Item.TYPE_ITEM && isChecked) {
                        if (logicEditor.o.c(item.text) || projectDataManager.c(javaName, item.text, eventName)) {
                            //noinspection ConstantConditions Item#inUseMessage can't be null if Item#type is Item#TYPE_ITEM
                            SketchwareUtil.toastError(Helper.getResString(item.inUseMessage), bB.TOAST_WARNING);
                            checkBoxHolder.checkBox.performClick();
                        }
                    }
                });
            } else {
                throw new IllegalStateException("Unknown view type " + viewType);
            }
        }

        @Override
        // RecyclerView.Adapter#getItemViewType(int)
        public int b(int position) {
            return data.get(position).type;
        }

        private class CheckBoxHolder extends RecyclerView.v {
            public final CheckBox checkBox;

            public CheckBoxHolder(View itemView) {
                super(itemView);
                checkBox = (CheckBox) itemView;
            }
        }

        private class TitleHolder extends RecyclerView.v {
            public final TextView title;

            public TitleHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView;
            }
        }
    }

    private static class Item {
        public static final int TYPE_TITLE = 0;
        public static final int TYPE_ITEM = 1;

        private final int type;
        private final String text;
        @StringRes
        private final Integer inUseMessage;

        private boolean isChecked = false;

        public Item(String title) {
            type = TYPE_TITLE;
            text = title;
            inUseMessage = null;
        }

        public Item(String itemName, @StringRes int inUseMessage) {
            type = TYPE_ITEM;
            text = itemName;
            this.inUseMessage = inUseMessage;
        }

        public int getType() {
            return type;
        }

        public String getText() {
            return text;
        }
    }
}
