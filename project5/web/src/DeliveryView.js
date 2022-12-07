import "./App.css"
import React, { useState, useEffect } from "react"
import {
    Stack,
    Button,
    Card,
    Grid,
    Badge,
    UnstyledButton,
    Group,
    Text,
    Flex,
    CloseButton,
    TextInput,
} from "@mantine/core"
import { IconBoxSeam } from "@tabler/icons"

function MainLink({ label }) {
    return (
        <UnstyledButton
            sx={(theme) => ({
                display: "block",
                width: "100%",
                padding: theme.spacing.xs,
                borderRadius: theme.radius.sm,
                marginBottom: "8px",
                color: theme.black,
                backgroundColor: theme.colors.gray[1],
                "&:hover": {
                    backgroundColor: theme.colors.gray[2],
                },
            })}
        >
            <Group>
                <Text size="sm">{label}</Text>
            </Group>
        </UnstyledButton>
    )
}

class DeliveryView extends React.Component {
    state = {
        orders: [],
        newItem: "",
    }

    componentDidMount() {
        this.getOrders()
        this.fetchInterval = setInterval(this.getOrders, 1000)
    }

    componentWillUnmount() {
        clearInterval(this.fetchInterval)
    }

    getOrders = async () => {
        const res = await fetch("http://localhost:8002/orders").then((res) =>
            res.json()
        )
        this.setState({
            orders: res.map((i) => {
                const [key, value] = Object.entries(i)[0]

                return {
                    ...value,
                    id: key,
                }
            }),
        })
    }

    render() {
        return (
            <Stack
                style={{
                    maxWidth: "300px",
                    margin: "auto",
                }}
            >
                {this.state.orders.map((order) => (
                    <Flex key={order.id}>
                        <MainLink label={order.items.join(",")} />
                        <Button
                            variant="subtle"
                            style={{ marginLeft: "8px", height: "42px" }}
                            color="green"
                            onClick={() => {
                                fetch("http://localhost:8002/deliveries", {
                                    method: "POST",
                                    headers: {
                                        "Content-Type": "text/plain",
                                    },
                                    body: order.id,
                                })
                            }}
                        >
                            <IconBoxSeam />
                        </Button>
                    </Flex>
                ))}
            </Stack>
        )
    }
}

export default DeliveryView
