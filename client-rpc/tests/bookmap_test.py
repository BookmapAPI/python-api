import unittest
import src.bookmap as bm


class BookmapOrderBookMethod(unittest.TestCase):

    def test_order_book_creation(self):
        book = bm.create_order_book()
        asks = book["asks"]
        bids = book["bids"]

        if bool(asks):
            self.fail("Asks are not empty")

        if bool(bids):
            self.fail("Bids are not empty")

        pass

    def test_order_book_update(self):
        book = bm.create_order_book()
        bm.on_depth(book, True, 99, 5)
        bm.on_depth(book, False, 100, 11)
        bm.on_depth(book, True, 95, 3)
        bm.on_depth(book, False, 101, 22)
        bm.on_depth(book, True, 99, 2)
        bm.on_depth(book, False, 101, 21)

        asks = book["asks"]
        bids = book["bids"]

        if len(bids) != 2:
            self.fail("Wrong number of bid levels")

        if len(asks) != 2:
            self.fail("Wrong number of ask levels")

        self.assertEqual(2, bids[99])
        self.assertEqual(3, bids[95])
        self.assertEqual(21, asks[101])
        self.assertEqual(11, asks[100])

    def test_bbo_order_book_request(self):
        book = bm.create_order_book()
        bm.on_depth(book, True, 99, 5)
        bm.on_depth(book, False, 100, 11)
        bm.on_depth(book, True, 95, 3)
        bm.on_depth(book, False, 101, 22)
        bm.on_depth(book, True, 99, 2)
        bm.on_depth(book, False, 101, 21)

        best_bid, best_ask = bm.get_bbo(book)

        self.assertEqual(99, best_bid[0])
        self.assertEqual(2, best_bid[1])
        self.assertEqual(100, best_ask[0])
        self.assertEqual(11, best_ask[1])

    def test_sub_order_book_request(self):
        book = bm.create_order_book()
        bm.on_depth(book, True, 99, 5)
        bm.on_depth(book, True, 95, 10)
        bm.on_depth(book, True, 97, 3)
        bm.on_depth(book, True, 98, 2)

        bm.on_depth(book, False, 100, 11)
        bm.on_depth(book, False, 101, 22)
        bm.on_depth(book, False, 102, 21)
        bm.on_depth(book, False, 104, 10)
        bm.on_depth(book, False, 105, 2)

        one_bid_sum, one_ask_sum = bm.get_sum(book, 1)
        self.assertEqual(11, one_ask_sum)
        self.assertEqual(5, one_bid_sum)

        two_bid_sum, two_ask_sum = bm.get_sum(book, 2)
        self.assertEqual(33, two_ask_sum)
        self.assertEqual(7, two_bid_sum)

        three_bid_sum, three_ask_sum = bm.get_sum(book, 3)
        self.assertEqual(54, three_ask_sum)
        self.assertEqual(10, three_bid_sum)

        four_bid_sum, four_ask_sum = bm.get_sum(book, 4)
        self.assertEqual(54, four_ask_sum)
        self.assertEqual(10, four_bid_sum)

        five_bid_sum, five_ask_sum = bm.get_sum(book, 5)
        self.assertEqual(64, five_ask_sum)
        self.assertEqual(20, five_bid_sum)

        six_bid_sum, six_ask_sum = bm.get_sum(book, 6)
        self.assertEqual(66, six_ask_sum)
        self.assertEqual(20, six_bid_sum)

    def test_mbo_order_book_creation(self):
        book = bm.create_mbo_book()
        orders = book["orders"]
        bids = book["mbp_book"]["bids"]
        asks = book["mbp_book"]["asks"]

        if bool(orders):
            self.fail("Orders are not empty")

        if bool(asks):
            self.fail("Asks are not empty")

        if bool(bids):
            self.fail("Bids are not empty")

        pass

    def test_mbo_order_book_get_all_order_ids(self):
        book = bm.create_mbo_book()
        bm.on_new_order(book, "1", True, 20, 1)
        bm.on_new_order(book, "2", True, 21, 2)
        bm.on_new_order(book, "3", True, 22, 3)

        ids = bm.get_all_order_ids(book)

        if len(ids) != 3:
            self.fail("Wrong number of orders")

        self.assertEqual("1", ids[0])
        self.assertEqual("2", ids[1])
        self.assertEqual("3", ids[2])

    def test_has_order(self):
        book = bm.create_mbo_book()
        bm.on_new_order(book, "1", True, 20, 1)
        bm.on_new_order(book, "2", True, 21, 2)
        bm.on_new_order(book, "3", True, 22, 3)

        self.assertTrue(bm.has_order(book, "1"))
        self.assertTrue(bm.has_order(book, "2"))
        self.assertTrue(bm.has_order(book, "3"))
        self.assertFalse(bm.has_order(book, "4"))

    def test_get_order(self):
        book = bm.create_mbo_book()
        bm.on_new_order(book, "1", True, 20, 1)
        bm.on_new_order(book, "2", False, 21, 2)
        bm.on_new_order(book, "3", True, 22, 3)

        order_id_1 = bm.get_order(book, "1")
        order_id_2 = bm.get_order(book, "2")
        order_id_3 = bm.get_order(book, "3")

        self.assertEqual((True, 20, 1), order_id_1)
        self.assertEqual((False, 21, 2), order_id_2)
        self.assertEqual((True, 22, 3), order_id_3)

    def test_get_order_price(self):
        book = bm.create_mbo_book()
        bm.on_new_order(book, "1", True, 20, 1)
        bm.on_new_order(book, "2", False, 21, 2)
        order_id_1_price = bm.get_order_price(book, "1")
        order_id_2_price = bm.get_order_price(book, "2")

        self.assertEqual(20, order_id_1_price)
        self.assertEqual(21, order_id_2_price)

    def test_get_order_size(self):
        book = bm.create_mbo_book()
        bm.on_new_order(book, "1", True, 20, 1)
        bm.on_new_order(book, "2", False, 21, 2)
        order_id_1_size = bm.get_order_size(book, "1")
        order_id_2_size = bm.get_order_size(book, "2")

        self.assertEqual(1, order_id_1_size)
        self.assertEqual(2, order_id_2_size)

    def test_get_order_side(self):
        book = bm.create_mbo_book()
        bm.on_new_order(book, "1", True, 20, 1)
        bm.on_new_order(book, "2", False, 21, 2)
        order_id_1_side = bm.get_order_side(book, "1")
        order_id_2_side = bm.get_order_side(book, "2")

        self.assertTrue(order_id_1_side)
        self.assertFalse(order_id_2_side)

    def test_mbo_order_book_replace(self):
        book = bm.create_mbo_book()
        bm.on_new_order(book, "1", True, 20, 1)
        bm.on_new_order(book, "2", True, 21, 2)
        bm.on_new_order(book, "3", True, 22, 3)
        bm.on_new_order(book, "4", False, 23, 1)
        bm.on_new_order(book, "5", False, 24, 2)
        bm.on_new_order(book, "6", False, 25, 3)
        bm.on_replace_order(book, "1", 19, 2)
        bm.on_replace_order(book, "6", 23, 3)

        orders = book["orders"]
        mbp_book = book["mbp_book"]
        asks = mbp_book["asks"]
        bids = mbp_book["bids"]

        if len(orders) != 6:
            self.fail("Orders size is wrong")

        if len(asks) != 2:
            self.fail("Wrong size of asks map")

        if len(bids) != 3:
            self.fail("Wrong size of bids map")

        self.assertEqual((True, 19, 2), orders["1"])
        self.assertEqual((True, 21, 2), orders["2"])
        self.assertEqual((True, 22, 3), orders["3"])
        self.assertEqual((False, 23, 1), orders["4"])
        self.assertEqual((False, 24, 2), orders["5"])
        self.assertEqual((False, 23, 3), orders["6"])

    def test_mbo_order_book_remove(self):
        book = bm.create_mbo_book()
        bm.on_new_order(book, "1", True, 20, 1)
        bm.on_new_order(book, "2", True, 21, 2)
        bm.on_new_order(book, "3", True, 22, 3)
        bm.on_new_order(book, "4", False, 23, 1)
        bm.on_new_order(book, "5", False, 24, 2)
        bm.on_new_order(book, "6", False, 25, 3)
        bm.on_remove_order(book, "1")
        bm.on_remove_order(book, "6")

        orders = book["orders"]
        mbp_book = book["mbp_book"]
        asks = mbp_book["asks"]
        bids = mbp_book["bids"]

        if len(orders) != 4:
            self.fail("Orders size is wrong")

        if len(asks) != 2:
            self.fail("Wrong size of asks map")

        if len(bids) != 2:
            self.fail("Wrong size of bids map")

        self.assertEqual((True, 21, 2), orders["2"])
        self.assertEqual((True, 22, 3), orders["3"])
        self.assertEqual((False, 23, 1), orders["4"])
        self.assertEqual((False, 24, 2), orders["5"])

    def test_error_getting_side_which_is_not_in_order_book(self):
        book = bm.create_mbo_book()
        order = bm.get_order(book, "1")
        self.assertEqual(None, order)

    @unittest.expectedFailure
    def test_error_due_to_order_duplicate(self):
        book = bm.create_mbo_book()
        bm.on_new_order(book, "1", True, 20, 1)
        bm.on_new_order(book, "1", True, 21, 2)

    @unittest.expectedFailure
    def test_error_due_to_replace_of_missing(self):
        book = bm.create_mbo_book()
        bm.on_replace_order(book, "1", 20, 1)

    @unittest.expectedFailure
    def test_error_due_to_remove_of_missing(self):
        book = bm.create_mbo_book()
        bm.on_remove_order(book, "1")

    @unittest.expectedFailure
    def test_error_getting_order_price_which_is_not_in_order_book(self):
        book = bm.create_mbo_book()
        bm.get_order_price(book, "1")

    @unittest.expectedFailure
    def test_error_getting_order_size_which_is_not_in_order_book(self):
        book = bm.create_mbo_book()
        bm.get_order_size(book, "1")

    @unittest.expectedFailure
    def test_error_getting_order_side_which_is_not_in_order_book(self):
        book = bm.create_mbo_book()
        bm.get_order_size(book, "1")
