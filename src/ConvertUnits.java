import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ConvertUnits {
    public static void main(String... args) throws IOException {
        String unitFrom = "";
        String unitTo = "";
        String newUnit;
        float result;
        float valueUnitFrom = 0.0F;
        float ruleUnitFromCm = 0f;
        float ruleUnitToCm = 0f;

        //Заполняем мап единицами по умолчанию в см
        HashMap<String, Float> allUnits = new HashMap<>();
        allUnits.put("m", 1000F);
        allUnits.put("cm", 1F);
        allUnits.put("in", 2.54F);
        allUnits.put("ft", 30.48F);

        try {
            File obj = new File("in.JSON");
            Scanner Reader = new Scanner(obj);
            String data = Reader.useDelimiter("@@@").next();

            int start = data.indexOf('\"') + 1;
            int end = data.indexOf('\"', start);
            boolean newUnits = false;

            while (start != 0 && end != -1) {
                //Добавляем в мап новые единицы и правила конвертации, указанные в JSON
                if (newUnits) {
                    newUnit = data.substring(start, end);
                    end = end - checkQuotes(newUnit,"new_convert");
                    start = data.indexOf(":", end + 1) + 1;
                    end = start + findValue(data.substring(start));

                    allUnits.put(newUnit, checkNumeric(data.substring(start, end)));
                } else {
                    switch (data.substring(start, end)) {
                        case ("unit"):
                            start = data.indexOf('\"', end + 1) + 1;
                            end = data.indexOf('\"', start);
                            unitFrom = data.substring(start, end);
                            end = end - checkQuotes(unitFrom,"unit");
                            break;
                        case ("value"):
                            start = data.indexOf(':', end + 1) + 1;
                            end = start + findValue(data.substring(start));
                            valueUnitFrom = checkNumeric(data.substring(start, end));
                            break;
                        case ("convert_to"):
                            start = data.indexOf('\"', end + 1) + 1;
                            end = data.indexOf('\"', start);
                            unitTo = data.substring(start, end);
                            end = end - checkQuotes(unitTo,"convert_to");
                            break;
                        case ("new_convert"):
                            newUnits = true;
                            break;
                    }
                }
                start = data.indexOf('\"', end + 1) + 1;
                end = data.indexOf('\"', start);
            }
            Reader.close();

            //Достаем из мапа значения входящих и выходящих единиц в см, если соответствие не найдено, или значение для перевода равно нулю, выдаем ошибку
            if (allUnits.get(unitTo) == null || allUnits.get(unitTo) == 0)
                System.out.println("Данные для единицы измерения, в которую осуществляется конвертация не заданы, либо не найден блок ее инициализации \"convert_to\"");
            else
                ruleUnitToCm = allUnits.get(unitTo);
            if (allUnits.get(unitFrom) == null || allUnits.get(unitFrom) == 0)
                System.out.println("Данные для единицы измерения, из которой осуществляется конвертация не заданы, либо не найден блок ее инициализации \"unit\"");
            else
                ruleUnitFromCm = allUnits.get(unitFrom);

            //
            if (valueUnitFrom == 0) System.out.println("Значение для единицы измерения, из которой осуществляется конвертация не задано, либо не найден блок его инициализации \"value\"");

            result = ruleUnitToCm != 0 ? ((float) Math.round((valueUnitFrom * ruleUnitFromCm) / ruleUnitToCm * 100) / 100) : 0;
            System.out.println(valueUnitFrom + " '" + unitFrom + "' = " + result + " '" + unitTo + "'");

            //Формируем JSON нужного формата
            writeJSON(Arrays.asList("{","\"unit\":\"" + unitTo + "\",","\"value\":" + result,"}"));

        } catch (FileNotFoundException e) {
            System.out.println("Файл \"in.JSON\" не найден");
        }
    }

    //Проверяем корректность введенных строковых значений
    public static int checkQuotes(String unitStringValue, String currentBloc) {
        if (unitStringValue.contains(",") || unitStringValue.contains("}")) {
            System.out.println("Пропущен символ \" в блоке \"" + currentBloc + "\"");
            return unitStringValue.length()+1;
        }
        return 0;
    }

    //Обрабатываем числовые значения
    public static int findValue(String unitNumericalValue) {
        int count = 0;
        for (char c : unitNumericalValue.substring(1).toCharArray()) {
            count++;
            if (!Character.isDigit(c) && c != '.' && c != ' ') break;
        }
        return count;
    }

    //Проверяем корректность введенных числовых значений
    public static float checkNumeric(String unitNumericalValue) {
        float currentUnitValue = 0f;
        unitNumericalValue = unitNumericalValue.trim();
        try {
            currentUnitValue = Float.parseFloat(unitNumericalValue);
        } catch (NumberFormatException e) {
            System.out.println("Проверьте корректность введенных числовых значений");
        }
        return currentUnitValue;
    }

    //Создаем, либо перезаписываем выходной JSON
    public static void writeJSON(List filingList) throws IOException {
        FileWriter writer = new FileWriter("out.JSON", false);
        for (Object o : filingList) writer.write(o + "\n");
        writer.close();
    }
}