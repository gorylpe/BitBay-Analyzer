package pl.dimzi.cryptocurrencyanalyzer.bitbay.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.enums.TradeType;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.model.Trade;
import pl.dimzi.cryptocurrencyanalyzer.bitbay.model.TradeBlock;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class TestRepository {
    @Mock
    private Connection conn;

    @Mock
    private Statement statementDefault;

    @Mock
    private PreparedStatement statementPreparedDefault;

    @Mock
    private PreparedStatement statementDateEndInside;
    @Mock
    private ResultSet resultDateEndInside;
    private int resultDateEndInsideIndex = -1;

    @Mock
    private PreparedStatement statementDateStartInside;
    @Mock
    private ResultSet resultDateStartInside;
    private int resultDateStartInsideIndex = -1;

    @Mock
    private PreparedStatement statementResult;
    private long resultDateStart;
    private long resultDateEnd;

    private TradeType type;
    private Repository repo;

    @Before
    public void setUp(){
        try {
            Mockito.when(conn.createStatement()).thenReturn(statementDefault);
            Mockito.when(statementDefault.execute(Matchers.anyString())).thenReturn(true);

            repo = new Repository(conn);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void shouldMergeInsideRange(){
        ArrayList<TradeBlock> dateEndInside = new ArrayList<>();
        dateEndInside.add(new TradeBlock(20, 40));
        ArrayList<TradeBlock> dateStartInside = new ArrayList<>();
        dateStartInside.add(new TradeBlock(20, 40));

        mergeBlocksMethodTest(TradeType.ETHPLN, 10, 70, 10, 70, dateEndInside, dateStartInside);
    }

    @Test
    public void shouldMergeLeftSideRange(){
        ArrayList<TradeBlock> dateEndInside = new ArrayList<>();
        dateEndInside.add(new TradeBlock(20, 40));
        ArrayList<TradeBlock> dateStartInside = new ArrayList<>();

        mergeBlocksMethodTest(TradeType.ETHPLN, 30, 70, 20, 70, dateEndInside, dateStartInside);
    }

    @Test
    public void shouldMergeRightSideRange(){
        ArrayList<TradeBlock> dateEndInside = new ArrayList<>();
        ArrayList<TradeBlock> dateStartInside = new ArrayList<>();
        dateStartInside.add(new TradeBlock(50, 100));

        mergeBlocksMethodTest(TradeType.ETHPLN, 30, 70, 30, 100, dateEndInside, dateStartInside);
    }

    @Test
    public void shouldMergeAllTypesRanges(){
        ArrayList<TradeBlock> dateEndInside = new ArrayList<>();
        dateEndInside.add(new TradeBlock(20, 40));
        dateEndInside.add(new TradeBlock(45, 55));
        ArrayList<TradeBlock> dateStartInside = new ArrayList<>();
        dateStartInside.add(new TradeBlock(60, 100));
        dateStartInside.add(new TradeBlock(45, 55));

        mergeBlocksMethodTest(TradeType.ETHPLN, 30, 70, 20, 100, dateEndInside, dateStartInside);
    }



    private void mergeBlocksMethodTest(TradeType type, long dateStart, long dateEnd, long expectedDateStart, long expectedDateEnd,
                                       ArrayList<TradeBlock> dateEndInside, ArrayList<TradeBlock> dateStartInside){
        try {
            resultDateEndInsideIndex = -1;
            resultDateStartInsideIndex = -1;

            Mockito.when(resultDateEndInside.next()).thenAnswer(e -> {
                resultDateEndInsideIndex++; return resultDateEndInsideIndex < dateEndInside.size();});
            Mockito.when(resultDateEndInside.getLong(1)).thenAnswer(e -> dateEndInside.get(resultDateEndInsideIndex).getDateStart());
            Mockito.when(resultDateEndInside.getLong(2)).thenAnswer(e -> dateEndInside.get(resultDateEndInsideIndex).getDateEnd());

            Mockito.when(resultDateStartInside.next()).thenAnswer(e -> {
                resultDateStartInsideIndex++; return resultDateStartInsideIndex < dateStartInside.size();});
            Mockito.when(resultDateStartInside.getLong(1)).thenAnswer(e -> dateStartInside.get(resultDateStartInsideIndex).getDateStart());
            Mockito.when(resultDateStartInside.getLong(2)).thenAnswer(e -> dateStartInside.get(resultDateStartInsideIndex).getDateEnd());

            Mockito.when(statementDateEndInside.executeQuery()).thenReturn(resultDateEndInside);
            Mockito.when(statementDateStartInside.executeQuery()).thenReturn(resultDateStartInside);

            Mockito.doNothing().when(statementPreparedDefault).setLong(Matchers.anyInt(), Matchers.anyLong());

            Mockito.doAnswer(e -> resultDateStart = (long)(e.getArguments()[1]))
                    .when(statementResult).setLong(Matchers.eq(1), Matchers.anyLong());

            Mockito.doAnswer(e -> resultDateEnd = (long)(e.getArguments()[1]))
                    .when(statementResult).setLong(Matchers.eq(2), Matchers.anyLong());

            Mockito.when(conn.prepareStatement(
                    "SELECT * FROM " + type.getTradeBlocksTableName()  + " WHERE dateEnd >= ? AND dateEnd <= ?"))
                    .thenReturn(statementDateEndInside);

            Mockito.when(conn.prepareStatement(
                    "SELECT * FROM " + type.getTradeBlocksTableName()  + " WHERE dateStart >= ? AND dateStart <= ?"))
                    .thenReturn(statementDateStartInside);

            Mockito.when(conn.prepareStatement(
                    "DELETE FROM " + type.getTradeBlocksTableName() + " WHERE dateEnd >= ? AND dateEnd <= ?"))
                    .thenReturn(statementPreparedDefault);

            Mockito.when(conn.prepareStatement(
                    "DELETE FROM " + type.getTradeBlocksTableName() + " WHERE dateStart >= ? AND dateStart <= ?"))
                    .thenReturn(statementPreparedDefault);

            Mockito.when(conn.prepareStatement(
                    "INSERT OR REPLACE INTO " + type.getTradeBlocksTableName() + " VALUES(?, ?)"))
                    .thenReturn(statementResult);

            Method method = Repository.class.getDeclaredMethod("mergeTradesBlocksWithNewTrade", TradeBlock.class, TradeType.class);
            method.setAccessible(true);
            method.invoke(repo, new TradeBlock(dateStart, dateEnd), type);

            Assert.assertEquals(expectedDateStart, resultDateStart);
            Assert.assertEquals(expectedDateEnd, resultDateEnd);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
