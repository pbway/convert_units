import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class convert_units {
    public static void main(String... args) throws IOException {
        String unit = "";
        String to = "";
        String new_unit;
        boolean new_units = false;
        float calcResult;
        float value = 0.0F;
        float unitFrom;
        float unitTo;

        //Заполняем мап единицами по умолчанию в см
        Map<String, Float> all_units = new HashMap<>();
        all_units.put("m", 1000F);
        all_units.put("cm", 1F);
        all_units.put("in", 2.54F);
        all_units.put("ft", 30.48F);

        try {
            File obj = new File("input.JSON");
            Scanner Reader = new Scanner(obj);
            String data = Reader.useDelimiter("@@@}").next();

            int start = data.indexOf('\"') + 1, end = data.indexOf('\"', start);
            while (start != 0 && end != -1) {
                //Добавляем в мап новые единицы и правила конвертации, указанные в JSON
                if (new_units) {
                    new_unit = data.substring(start, end);
                    start = data.indexOf('\"', end + 1) + 1;
                    end = data.indexOf('\"', start);
                    all_units.put(new_unit, Float.parseFloat(data.substring(start, end)));
                } else {
                    switch (data.substring(start, end)) {
                        case ("unit"):
                            start = data.indexOf('\"', end + 1) + 1;
                            end = data.indexOf('\"', start);
                            unit = data.substring(start, end);
                            break;
                        case ("value"):
                            start = data.indexOf('\"', end + 1) + 1;
                            end = data.indexOf('\"', start);
                            value = Float.parseFloat(data.substring(start, end));
                            break;
                        case ("convert_to"):
                            start = data.indexOf('\"', end + 1) + 1;
                            end = data.indexOf('\"', start);
                            to = data.substring(start, end);
                            break;
                        case ("new_convert"):
                            new_units = true;
                            break;
                    }
                }
                start = data.indexOf('\"', end + 1) + 1;
                end = data.indexOf('\"', start);
            }
            Reader.close();

            //Достаем из мапа значения входящих и выходящих единиц в см
            unitFrom = all_units.get(unit);
            unitTo = all_units.get(to);

            calcResult = (float) Math.round((value * unitFrom) / unitTo * 100) / 100;
            System.out.println(value + " '" + unit + "' = " + calcResult + " '" + to + "'");
            List<String> list = Arrays.asList("{","\"unit\":\"" + to + "\",","\"value\":" + calcResult,"}");
            writeJSON(list);

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        }
    }

    //Запись в JSON
    public static void writeJSON(List l) throws IOException {
        FileWriter writer = new FileWriter("out.JSON", false);
        for (Object o : l) writer.write(o + "\n");
        writer.close();
    }
}